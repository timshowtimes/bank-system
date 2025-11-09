package kz.timshowtime.cashservice.service;


import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(org.mockito.junit.jupiter.MockitoExtension.class)
public class CashServiceTest {

    @Mock
    private WebClient webClient;
    @Mock
    private WebClient.RequestBodyUriSpec uriSpec;
    @Mock
    private WebClient.RequestBodySpec bodySpec;
    @Mock
    private WebClient.ResponseSpec responseSpec;
    @Mock
    private NotificationClient notificationClient;

    @InjectMocks
    private CashService cashService;


    @Test
    void depositWhenSuccessShouldCallWebClientAndNotify() {
        Long accountId = 1L;
        BigDecimal amount = new BigDecimal("100.00");

        when(webClient.post()).thenReturn(uriSpec);
        when(uriSpec.uri(anyString(), anyLong(), any(BigDecimal.class))).thenReturn(bodySpec);
        when(bodySpec.retrieve()).thenReturn(responseSpec);
        when(responseSpec.toBodilessEntity()).thenReturn(Mono.empty());

        String result = cashService.deposit(accountId, amount);

        assertThat(result).isEqualTo("Deposit successful");
        verify(webClient).post();
        verify(responseSpec).toBodilessEntity();
        verify(notificationClient).notifySuccess(accountId, "Deposit", amount);
    }

    @Test
    void withdrawSuccessShouldReturnOkAndNotifySuccess() {
        Long accountId = 1L;
        BigDecimal amount = new java.math.BigDecimal("100.00");

        when(webClient.post()).thenReturn(uriSpec);
        when(uriSpec.uri(anyString(), eq(accountId), eq(amount))).thenReturn(bodySpec);

        when(bodySpec.exchangeToMono(any()))
                .thenReturn(Mono.just("OK"));

        String result = cashService.withdraw(accountId, amount);

        assertThat(result).isEqualTo("OK");
        verify(webClient).post();
        verify(notificationClient).notifySuccess(accountId, "Withdraw", amount);
        verifyNoMoreInteractions(notificationClient);
    }

    @Test
    void withdrawBusinessErrorShouldReturnBusinessErrorStringAndNotifySuccess() {
        Long accountId = 2L;
        BigDecimal amount = new java.math.BigDecimal("50.00");
        String businessBody = "{\"message\":\"Недостаточно средств на счете\",\"error\":\"InsufficientBalance\"}";

        when(webClient.post()).thenReturn(uriSpec);
        when(uriSpec.uri(anyString(), eq(accountId), eq(amount))).thenReturn(bodySpec);

        when(bodySpec.exchangeToMono(any()))
                .thenReturn(Mono.just("BUSINESS_ERROR:" + businessBody));

        String result = cashService.withdraw(accountId, amount);

        assertThat(result).startsWith("BUSINESS_ERROR:");
        assertThat(result).contains("Недостаточно средств");
        verify(notificationClient).notifySuccess(accountId, "Withdraw", amount);
    }
}
