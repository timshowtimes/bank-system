package kz.timshowtime.cashservice.config;

import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Profile;
import org.springframework.web.reactive.function.client.WebClient;

@TestConfiguration
@Profile("test")
public class TestWebClientConfig {

    @Bean
    public WebClient webClient() {
        return WebClient.builder().build();
    }
}
