package kz.timshowtime.frontendservice.controller;

import jakarta.servlet.http.HttpServletRequest;
import kz.timshowtime.frontendservice.dto.AccountProfileDto;
import kz.timshowtime.frontendservice.dto.ChangePasswordRequest;
import kz.timshowtime.frontendservice.dto.TransferRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.RestTemplate;

import java.math.BigDecimal;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/frontend")
@RequiredArgsConstructor
@Slf4j
public class FrontFetchController {

    private final RestTemplate restTemplate;

    @GetMapping(value = "/get-account", produces = MediaType.APPLICATION_JSON_VALUE)
    public AccountProfileDto getAccountProfile(HttpServletRequest request) {
        String login = request.getSession().getAttribute("login").toString();
        HttpHeaders headers = new HttpHeaders();
        headers.add("X-User-Login", login);
        HttpEntity<Void> entity = new HttpEntity<>(headers);

        ResponseEntity<AccountProfileDto> response = restTemplate.exchange(
                "http://gateway-service/api/v1/accounts/me",
                HttpMethod.GET,
                entity,
                AccountProfileDto.class
        );

        return response.getBody();
    }

    @PostMapping("/deposit/{accountId}")
    public ResponseEntity<String> deposit(
            @PathVariable("accountId") Long accountId,
            @RequestParam("amount") BigDecimal amount,
            HttpServletRequest request
    ) {
        ResponseEntity<String> response = restTemplate.postForEntity(
                "http://gateway-service/api/v1/cash/deposit/" + accountId + "?amount=" + amount,
                null,
                String.class
        );

        return ResponseEntity.status(response.getStatusCode()).body(response.getBody());
    }


    @PostMapping("/{accountId}/change-password")
    public ResponseEntity<String> changePassword(@PathVariable("accountId") Long accountId,
                                                 @RequestBody ChangePasswordRequest passwordRequest) {
        try {
            ResponseEntity<String> response = restTemplate.postForEntity(
                    "http://gateway-service/api/v1/accounts/" + accountId + "/change-password",
                    passwordRequest,
                    String.class
            );

            return ResponseEntity.status(response.getStatusCode()).body(response.getBody());
        } catch (HttpClientErrorException | HttpServerErrorException e) {
            return ResponseEntity.status(e.getStatusCode()).body(e.getResponseBodyAsString());
        }
    }

    @DeleteMapping("/{id}/delete-account")
    public ResponseEntity<String> deleteAccount(@PathVariable("id") Long id) {

        try {
            ResponseEntity<String> response = restTemplate.exchange(
                    "http://gateway-service/api/v1/accounts/" + id,
                    HttpMethod.DELETE,
                    null,
                    String.class
            );

            return ResponseEntity.status(response.getStatusCode()).body(response.getBody());
        } catch (HttpClientErrorException | HttpServerErrorException e) {
            return ResponseEntity.status(e.getStatusCode()).body(e.getResponseBodyAsString());
        }
    }

    @PutMapping("/{id}/update-profile")
    public ResponseEntity<String> updateProfile(
            @PathVariable("id") Long id,
            @RequestBody AccountProfileDto dto
    ) {
        try {
            ResponseEntity<String> response = restTemplate.exchange(
                    "http://gateway-service/api/v1/accounts/" + id,
                    HttpMethod.PUT,
                    new HttpEntity<>(dto),
                    String.class
            );

            return ResponseEntity.status(response.getStatusCode()).body(response.getBody());
        } catch (HttpClientErrorException | HttpServerErrorException e) {
            return ResponseEntity.status(e.getStatusCode()).body(e.getResponseBodyAsString());
        }
    }

    @PostMapping("/withdraw/{accountId}")
    public ResponseEntity<String> withdraw(
            @PathVariable("accountId") Long accountId,
            @RequestParam("amount") BigDecimal amount,
            HttpServletRequest request
    ) {
        try {
            ResponseEntity<String> response = restTemplate.postForEntity(
                    "http://gateway-service/api/v1/cash/withdraw/" + accountId + "?amount=" + amount,
                    null,
                    String.class
            );

            return ResponseEntity.status(response.getStatusCode()).body(response.getBody());
        } catch (HttpClientErrorException | HttpServerErrorException e) {
            return ResponseEntity
                    .status(e.getStatusCode())
                    .body(e.getResponseBodyAsString());
        }
    }

    @PostMapping("/transfer")
    public ResponseEntity<String> transfer(@RequestBody TransferRequest request) {
        try {
            request.setTransferId(UUID.randomUUID());

            ResponseEntity<String> response = restTemplate.postForEntity(
                    "http://gateway-service/api/v1/transfer",
                    request,
                    String.class
            );

            return ResponseEntity
                    .status(response.getStatusCode())
                    .body(response.getBody());

        } catch (HttpClientErrorException e) {
            log.error("❌ Ошибка при переводе: {}", e.getResponseBodyAsString());
            return ResponseEntity
                    .status(e.getStatusCode())
                    .body(e.getResponseBodyAsString().trim());
        } catch (Exception e) {
            log.error("💥 Неожиданная ошибка при переводе", e);
            return ResponseEntity
                    .status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Внутренняя ошибка фронтенда: " + e.getMessage());
        }

    }
}
