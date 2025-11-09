package kz.timshowtime.cashservice.contract;

import kz.timshowtime.cashservice.CashServiceApplication;
import kz.timshowtime.cashservice.config.TestNoSecurityConfig;
import kz.timshowtime.cashservice.config.TestWebClientConfig;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.cloud.contract.stubrunner.spring.AutoConfigureStubRunner;
import org.springframework.cloud.contract.stubrunner.spring.StubRunnerProperties;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

@SpringBootTest(
        webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
        classes = {CashServiceApplication.class, TestNoSecurityConfig.class, TestWebClientConfig.class}
)
@ActiveProfiles("test")
@AutoConfigureStubRunner(
        ids = {"kz.timshowtime:accounts-service:+:stubs:8085"},
        stubsMode = StubRunnerProperties.StubsMode.LOCAL
)
public class CashServiceContractTest {

    @Autowired
    private TestRestTemplate restTemplate;

    @DynamicPropertySource
    static void overrideProps(DynamicPropertyRegistry registry) {
        registry.add("services.accounts.url", () -> "http://localhost:8085");
    }

    @Test
    void depositShouldSucceedAgainstStub() {
        ResponseEntity<String> resp = restTemplate.postForEntity(
                "/api/v1/cash/deposit/1?amount=500",
                null,
                String.class
        );

        assertThat(resp.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(resp.getBody()).contains("Deposit successful");
    }

    @Test
    void withdrawShouldSucceedAgainstStub() {
        ResponseEntity<String> resp = restTemplate.postForEntity(
                "/api/v1/cash/withdraw/1?amount=500",
                null,
                String.class
        );

        assertThat(resp.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(resp.getBody()).contains("{\"message\":\"OK\"}");
    }
}
