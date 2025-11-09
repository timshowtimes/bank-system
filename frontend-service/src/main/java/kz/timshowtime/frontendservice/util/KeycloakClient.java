package kz.timshowtime.frontendservice.util;

import kz.timshowtime.frontendservice.dto.TokenResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.Map;

@Component
@RequiredArgsConstructor
public class KeycloakClient {

    @Value("${keycloak.token-uri}")
    private String tokenUri;

    @Value("${keycloak.client-id}")
    private String clientId;

    @Value("${keycloak.client-secret}")
    private String clientSecret;


    public TokenResponse getServiceTokenWithExpiry() {
        WebClient webClient = WebClient.builder().build();
        Map response = webClient.post()
                .uri(tokenUri)
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .body(BodyInserters.fromFormData("grant_type", "client_credentials")
                        .with("client_id", clientId)
                        .with("client_secret", clientSecret))
                .retrieve()
                .bodyToMono(Map.class)
                .block();

        TokenResponse token = new TokenResponse();
        token.setAccessToken((String) response.get("access_token"));
        token.setExpiresIn(((Number) response.get("expires_in")).longValue());
        return token;
    }
}