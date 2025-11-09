package kz.timshowtime.cashservice.integration;

import kz.timshowtime.cashservice.CashServiceApplication;
import kz.timshowtime.cashservice.config.TestWebClientConfig;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.cloud.contract.wiremock.AutoConfigureWireMock;
import org.springframework.context.annotation.Bean;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.test.context.ActiveProfiles;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

@SpringBootTest(
        webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
        classes = {
                CashServiceApplication.class,
                TestWebClientConfig.class,
                CashServiceIntegrationTest.TestSecurityConfig.class
        }
)
@AutoConfigureWireMock(port = 9999)
@ActiveProfiles("test")
class CashServiceIntegrationTest {

    @LocalServerPort
    private int port;

    @Autowired
    private TestRestTemplate restTemplate;

    @TestConfiguration
    static class TestSecurityConfig {
        @Bean
        public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
            http
                    .csrf(AbstractHttpConfigurer::disable)
                    .authorizeHttpRequests(auth -> auth.anyRequest().permitAll());
            return http.build();
        }
    }

    // ===> Success Deposit
    @Test
    void shouldPerformDepositSuccessfully() {
        stubFor(post(urlPathMatching("/api/v1/accounts/.*"))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withBody("OK")));

        ResponseEntity<String> response = restTemplate.postForEntity(
                "http://localhost:" + port + "/api/v1/cash/deposit/1?amount=500",
                null,
                String.class
        );

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isEqualTo("Deposit successful");

        verify(postRequestedFor(urlMatching("/api/v1/accounts/.*")));
    }

    // ===> Insufficient Funds test
    @Test
    void shouldHandleInsufficientFunds() {
        stubFor(post(urlPathMatching("/api/v1/accounts/.*"))
                .willReturn(aResponse()
                        .withStatus(400)
                        .withBody("{\"message\":\"Недостаточно средств на счете\"}")
                        .withHeader("Content-Type", "application/json")));

        ResponseEntity<String> response = restTemplate.postForEntity(
                "http://localhost:" + port + "/api/v1/cash/withdraw/1?amount=999999",
                null,
                String.class
        );

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody()).contains("Недостаточно средств");

        verify(postRequestedFor(urlMatching("/api/v1/accounts/.*")));
    }

    // ===> Fallbacks check
    @Test
    void shouldTriggerFallbackOnServerError() {
        stubFor(post(urlPathMatching("/api/v1/accounts/.*"))
                .willReturn(aResponse()
                        .withStatus(500)
                        .withBody("{\"error\":\"Internal Server Error\"}")));

        stubFor(post(urlEqualTo("/api/v1/fallbacks/account"))
                .willReturn(aResponse()
                        .withStatus(200)));

        ResponseEntity<String> response = restTemplate.postForEntity(
                "http://localhost:" + port + "/api/v1/cash/deposit/1?amount=500",
                null,
                String.class
        );

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.SERVICE_UNAVAILABLE);
        assertThat(response.getBody()).contains("Service Unavailable");

        verify(postRequestedFor(urlMatching("/api/v1/accounts/.*")));
        verify(postRequestedFor(urlEqualTo("/api/v1/fallbacks/account")));
    }
}