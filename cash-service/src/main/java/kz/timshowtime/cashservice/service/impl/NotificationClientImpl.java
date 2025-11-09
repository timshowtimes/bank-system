package kz.timshowtime.cashservice.service.impl;

import kz.timshowtime.cashservice.service.NotificationClient;
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
    public void notifySuccess(Long fromAccount, String type, BigDecimal amount) {
        send(type + " success", "COMPLETED", fromAccount, amount);
    }

    @Override
    public void notifyFailure(Long fromAccount, BigDecimal amount, String status) {
        send("Cash operation failed", status, fromAccount, amount);
    }

    private void send(String message, String status, Long from, BigDecimal amount) {
        Map<String, Object> body = Map.of(
                "fromAccount", from,
                "message", message + " (" + amount + ")",
                "status", status
        );

        try {
//            restTemplate.postForObject(
//                    "http://gateway-service/api/v1/notifications",
//                    body,
//                    Void.class
//            );
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
