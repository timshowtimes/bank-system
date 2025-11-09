package kz.timshowtime.transferservice.service;

import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.retry.annotation.Retry;
import io.github.resilience4j.timelimiter.annotation.TimeLimiter;
import kz.timshowtime.transferservice.dto.TransferDto;
import kz.timshowtime.transferservice.dto.TransferRequest;
import kz.timshowtime.transferservice.exceptions.BusinessException;
import kz.timshowtime.transferservice.model.Transfer;
import kz.timshowtime.transferservice.repository.TransferRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.util.UriComponentsBuilder;
import reactor.core.publisher.Mono;

import java.net.URI;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

@Service
@Slf4j
@RequiredArgsConstructor
public class TransferService {

    private final TransferRepository transferRepository;
    private final WebClient webClient;
    private final NotificationClient notificationClient;

    @Transactional
    @CircuitBreaker(name = "cashServiceCircuitBreaker", fallbackMethod = "transferFallback")
    @Retry(name = "cashServiceRetry")
    @TimeLimiter(name = "cashServiceTimeLimiter")
    public CompletableFuture<TransferDto> executeTransfer(TransferRequest req) {
        return CompletableFuture.supplyAsync(() -> {
            log.info("Starting transfer {}", req.getTransferId());

            Optional<Transfer> existing = transferRepository.findByTransferId(req.getTransferId());
            if (existing.isPresent()) {
                log.info("Transfer {} already exists, returning existing record", req.getTransferId());
                return mapToDto(existing.get());
            }

            Long toAccountId;

            try {
                toAccountId = webClient.get()
                        .uri("http://accounts-service/api/v1/accounts/find/{login}", req.getToAccount())
                        .retrieve()
                        .onStatus(HttpStatusCode::is4xxClientError, response ->
                                response.bodyToMono(String.class)
                                        .flatMap(errorBody -> Mono.error(
                                                new BusinessException(
                                                        "{\"message\": \"Пользователь " + req.getToAccount() + " не найден\"}"
                                                )
                                        ))
                        )
                        .onStatus(HttpStatusCode::is5xxServerError, response ->
                                response.bodyToMono(String.class)
                                        .flatMap(errorBody -> Mono.error(
                                                new RuntimeException("Server error " + errorBody)
                                        ))
                        )
                        .bodyToMono(Long.class)
                        .block();
            } catch (BusinessException e) {
                log.warn("Business exception during transfer: {}", e.getMessage());

                TransferDto errorDto = new TransferDto();
                errorDto.setStatus("FAILED");
                errorDto.setMessage(e.getMessage());
                return errorDto;
            }

            Transfer transfer = Transfer.builder()
                    .transferId(req.getTransferId())
                    .fromAccount(req.getFromAccount())
                    .toAccount(toAccountId)
                    .amount(req.getAmount())
                    .status("PENDING")
                    .build();
            transfer = transferRepository.save(transfer);

            boolean debitDone = false;

            req.setToAccountId(toAccountId);

            try {
                webClient.post()
                        .uri("http://cash-service/api/v1/cash/withdraw/{accountId}?amount={amount}",
                                req.getFromAccount(), req.getAmount())
                        .retrieve()
                        .onStatus(HttpStatusCode::is4xxClientError, response ->
                                response.bodyToMono(String.class)
                                        .flatMap(errorBody -> Mono.error(
                                                new BusinessException(errorBody)
                                        ))
                        )
                        .toBodilessEntity()
                        .block();
                debitDone = true;

                webClient.post()
                        .uri("http://cash-service/api/v1/cash/deposit/{accountId}?amount={amount}",
                                req.getToAccountId(), req.getAmount())
                        .retrieve()
                        .toBodilessEntity()
                        .block();

                transfer.setStatus("COMPLETED");
                transferRepository.save(transfer);

                notificationClient.notifySuccess(req.getFromAccount(), toAccountId, req.getAmount());

                return mapToDto(transfer);
            } catch (BusinessException e) {
                TransferDto errorDto = new TransferDto();
                errorDto.setStatus("FAILED");
                errorDto.setMessage(e.getMessage());
                return errorDto;
            } catch (Exception e) {
                if (debitDone) {
                    try {
                        webClient.post()
                                .uri("http://cash-service/api/v1/cash/deposit/{accountId}?amount={amount}",
                                        req.getFromAccount(), req.getAmount())
                                .retrieve()
                                .toBodilessEntity()
                                .block();
                        transfer.setStatus("COMPENSATED");
                    } catch (Exception rollbackEx) {
                        transfer.setStatus("FAILED");
                    }
                } else {
                    transfer.setStatus("FAILED");
                }
                // notify failure
                notificationClient.notifyFailure(req.getFromAccount(), toAccountId, req.getAmount(), transfer.getStatus());

                throw new RuntimeException("Transfer failed, status=" + transfer.getStatus(), e);
            }
        });
    }

    private CompletableFuture<TransferDto> transferFallback(TransferRequest req, Throwable throwable) {
        log.warn("Fallback triggered for transfer {} due to {}", req.getTransferId(), throwable.getMessage());
        notificationClient.notifyFailure(req.getFromAccount(), req.getToAccountId(),
                req.getAmount(), "FALLBACK [From Transfer Service]");

        try {
            Map<String, Object> payload = Map.of(
                    "transferIf", req.getTransferId(),
                    "fromAccount", req.getFromAccount(),
                    "toAccount", req.getToAccount(),
                    "amount", req.getAmount(),
                    "reason", throwable.getMessage()
            );

            webClient.post()
                    .uri("http://stub-service/api/v1/fallbacks/transfer")
                    .bodyValue(payload)
                    .retrieve()
                    .toEntity(String.class)
                    .block();
            log.info("Transfer fallback sent to stub-service for {}", req.getTransferId());
        } catch (Exception e) {
            log.error("❌ Failed to send transfer fallback to stub-service: {}", e.getMessage());
        }

        TransferDto dto = new TransferDto(
                req.getTransferId(),
                req.getFromAccount(),
                req.getToAccountId(),
                req.getAmount(),
                "FALLBACK",
                throwable.getMessage()
        );
        return CompletableFuture.completedFuture(dto);
    }

    private TransferDto mapToDto(Transfer t) {
        return TransferDto.builder()
                .transferId(t.getTransferId())
                .fromAccount(t.getFromAccount())
                .toAccount(t.getToAccount())
                .amount(t.getAmount())
                .status(t.getStatus())
                .build();
    }
}