package kz.timshowtime.frontendservice.config;

import kz.timshowtime.frontendservice.util.KeycloakTokenManager;
import lombok.RequiredArgsConstructor;
import org.springframework.cloud.client.loadbalancer.LoadBalanced;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.client.RestTemplate;

@Configuration
@RequiredArgsConstructor
public class RestTemplateConfig {

    private final KeycloakTokenManager keycloakTokenManager;


    @Bean
    @LoadBalanced
    public RestTemplate restTemplate() {
        RestTemplate restTemplate = new RestTemplate();

        restTemplate.getInterceptors().add(((request, body, execution) -> {
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            if (auth != null && auth.getCredentials() != null) {
                String token = keycloakTokenManager.getValidAccessToken();
                request.getHeaders().setBearerAuth(token);
            }
            return execution.execute(request, body);
        }));

        return restTemplate;
    }
}
