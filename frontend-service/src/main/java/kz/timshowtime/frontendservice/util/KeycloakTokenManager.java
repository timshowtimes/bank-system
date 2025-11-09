package kz.timshowtime.frontendservice.util;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class KeycloakTokenManager {

    private final KeycloakClient keycloakClient;

    private String accessToken;
    private long expiresAt = 0;

    public synchronized String getValidAccessToken() {
        long now = System.currentTimeMillis();
        if (accessToken == null || now > expiresAt) {
            var tokenResponse = keycloakClient.getServiceTokenWithExpiry();
            this.accessToken = tokenResponse.getAccessToken();
            this.expiresAt = now + tokenResponse.getExpiresIn() * 1000 - 5000;
        }
        return accessToken;
    }
}