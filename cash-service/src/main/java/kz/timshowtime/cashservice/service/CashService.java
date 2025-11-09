package kz.timshowtime.cashservice.service;

import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.retry.annotation.Retry;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.server.ResponseStatusException;
import reactor.core.publisher.Mono;

import java.math.BigDecimal;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class CashService {

    private final WebClient webClient;
    private final NotificationClient notificationClient;
    @Value("${services.accounts.url}") private String accountsServiceUrl;
    @Value("${services.stub.url}") private String stubServiceUrl;

    @CircuitBreaker(name = "accountServiceCircuitBreaker", fallbackMethod = "cashFallback")
    @Retry(name = "accountServiceRetry")
    public String deposit(Long accountId, BigDecimal amount) {
        log.info("💰 Depositing {} to account {}", amount, accountId);
        webClient.post()
                .uri(accountsServiceUrl + "/api/v1/accounts/{id}/add-balance?amount={amount}", accountId, amount)
                .retrieve()
                .toBodilessEntity()
                .block();
        notificationClient.notifySuccess(accountId, "Deposit", amount);
        return "Deposit successful";
    }

    @CircuitBreaker(name = "accountServiceCircuitBreaker", fallbackMethod = "cashFallback")
    @Retry(name = "accountServiceRetry")
    public String withdraw(Long accountId, BigDecimal amount) {
        log.info("💸 Withdrawing {} from account {}", amount, accountId);

        try {

            return webClient.post()
                    .uri(accountsServiceUrl + "/api/v1/accounts/{id}/reduce-balance?amount={amount}", accountId, amount)
                    .exchangeToMono(res -> {
                        if (res.statusCode() == HttpStatus.OK) {
                            return Mono.just("OK");
                        } else if (res.statusCode().is5xxServerError()) {
                            return Mono.error(new RuntimeException("Server Error:  " + res.statusCode()));
                        } else if (res.statusCode() == HttpStatus.BAD_REQUEST) {
                            return res.bodyToMono(String.class)
                                    .map(body -> "BUSINESS_ERROR:" + body);
                        } else {
                            return Mono.error(new RuntimeException("Unexpected error:" + res.statusCode()));
                        }
                    })
                    .doOnSuccess(res -> notificationClient.notifySuccess(accountId, "Withdraw", amount))
                    .block();

        } catch (Exception e) {
            log.error("System error: {}", e.getMessage());
            notificationClient.notifyFailure(accountId, amount, e.getMessage());
            throw new ResponseStatusException(HttpStatus.SERVICE_UNAVAILABLE, "Account service unavailable", e);
        }
    }

    public String cashFallback(Long accountId, BigDecimal amount, Throwable throwable) {
        log.warn("⚠️ Account Service unavailable, sending fallback request to stub-service");

        try {
            Map<String, Object> fallbackRequest = Map.of(
                    "accountId", accountId,
                    "amount", amount,
                    "reason", throwable == null ? "unknown" : throwable.getMessage()
            );

            webClient.post()
                    .uri(stubServiceUrl + "/api/v1/fallbacks/account")
                    .bodyValue(fallbackRequest)
                    .retrieve()
                    .toBodilessEntity()
                    .block();

        } catch (Exception e) {
            log.error("❌ Fallback request to stub-service failed: {}", e.getMessage());
            return "Fallback failed";
        }

        throw new ResponseStatusException(HttpStatus.SERVICE_UNAVAILABLE,
                "Account service unavailable", throwable);

    }
}