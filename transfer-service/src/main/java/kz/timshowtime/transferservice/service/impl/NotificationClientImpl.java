package kz.timshowtime.transferservice.service.impl;

import kz.timshowtime.transferservice.service.NotificationClient;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.reactive.function.client.WebClient;

import java.math.BigDecimal;
import java.util.Map;

@Component
@Slf4j
@RequiredArgsConstructor
public class NotificationClientImpl implements NotificationClient {

    private final WebClient webClient;

    @Override
    public void notifySuccess(Long fromAccount, Long toAccount, BigDecimal amount) {
        send("Transfer success", "COMPLETED", fromAccount, toAccount, amount);
    }

    @Override
    public void notifyFailure(Long fromAccount, Long toAccount, BigDecimal amount, String status) {
        send("Transfer failed", status, fromAccount, toAccount, amount);
    }

    private void send(String message, String status, Long from, Long to, BigDecimal amount) {
        Map<String, Object> body = Map.of(
                "fromAccount", from,
                "toAccount", to,
                "message", message + " (" + amount + ")",
                "status", status
        );

        try {
            webClient.post()
                    .uri("http://notifications-service/api/v1/notifications")
                    .bodyValue(body)
                    .retrieve()
                    .toBodilessEntity()
                    .block();

        } catch (Exception e) {
            log.warn("Failed to send notification: {}", e.getMessage());
        }
    }


}
