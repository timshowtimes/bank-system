package kz.timshowtime.transferservice.service;


import kz.timshowtime.transferservice.dto.TransferDto;
import kz.timshowtime.transferservice.dto.TransferRequest;
import kz.timshowtime.transferservice.model.Transfer;
import kz.timshowtime.transferservice.repository.TransferRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.springframework.http.HttpStatus;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.math.BigDecimal;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(org.mockito.junit.jupiter.MockitoExtension.class)
public class TransferServiceTest {

    @Mock
    private TransferRepository transferRepository;
    @Mock
    private WebClient webClient;
    @Mock
    private NotificationClient notificationClient;

    @InjectMocks
    private TransferService transferService;

    @Mock
    private WebClient.RequestHeadersUriSpec getUriSpec;
    @Mock
    private WebClient.RequestHeadersSpec getHeadersSpec;
    @Mock
    private WebClient.ResponseSpec getResponseSpec;

    @Mock
    private WebClient.RequestBodyUriSpec postUriSpec;
    @Mock
    private WebClient.RequestBodySpec postHeadersSpec;
    @Mock
    private WebClient.ResponseSpec postResponseSpec;

    private final UUID transferId = UUID.randomUUID();

    private TransferRequest mockRequest() {
        TransferRequest req = new TransferRequest();
        req.setTransferId(transferId);
        req.setFromAccount(1L);
        req.setToAccount("receiver");
        req.setAmount(BigDecimal.valueOf(100));
        return req;
    }

    @Test
    void executeTransfer_successfulTransfer() throws Exception {
        TransferRequest req = mockRequest();

        Transfer transfer = Transfer.builder()
                .transferId(req.getTransferId())
                .fromAccount(req.getFromAccount())
                .toAccount(2L)
                .amount(req.getAmount())
                .status("PENDING")
                .build();

        when(transferRepository.findByTransferId(any())).thenReturn(Optional.empty());
        when(transferRepository.save(any())).thenReturn(transfer);

        // ===> mock GET accountId
        when(webClient.get()).thenReturn(getUriSpec);
        when(getUriSpec.uri(anyString(), anyString())).thenReturn(getHeadersSpec);
        when(getHeadersSpec.retrieve()).thenReturn(getResponseSpec);
        when(getResponseSpec.onStatus(any(), any())).thenReturn(getResponseSpec);
        when(getResponseSpec.bodyToMono(Long.class)).thenReturn(Mono.just(2L));

        // ===> mock POST withdraw
        when(webClient.post()).thenReturn(postUriSpec);
        when(postUriSpec.uri(contains("/withdraw"), anyLong(), any(BigDecimal.class)))
                .thenReturn(postHeadersSpec);
        when(postHeadersSpec.retrieve()).thenReturn(postResponseSpec);
        when(postResponseSpec.onStatus(any(), any())).thenReturn(postResponseSpec);
        when(postResponseSpec.toBodilessEntity()).thenReturn(Mono.empty());

        // ===> mock POST deposit
        when(postUriSpec.uri(contains("/deposit"), anyLong(), any(BigDecimal.class)))
                .thenReturn(postHeadersSpec);
        when(postHeadersSpec.retrieve()).thenReturn(postResponseSpec);
        when(postResponseSpec.onStatus(any(), any())).thenReturn(postResponseSpec);
        when(postResponseSpec.toBodilessEntity()).thenReturn(Mono.empty());

        CompletableFuture<TransferDto> resultFuture = transferService.executeTransfer(req);
        TransferDto result = resultFuture.get();

        assertThat(result).isNotNull();
        assertThat(result.getStatus()).isEqualTo("COMPLETED");

        verify(notificationClient, times(1))
                .notifySuccess(eq(1L), eq(2L), eq(BigDecimal.valueOf(100)));

        verify(transferRepository, atLeastOnce()).save(any());
    }
}
