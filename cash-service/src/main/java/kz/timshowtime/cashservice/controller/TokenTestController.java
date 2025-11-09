package kz.timshowtime.cashservice.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.authority.AuthorityUtils;
import org.springframework.security.oauth2.client.OAuth2AuthorizeRequest;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClient;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClientManager;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/test")
@RequiredArgsConstructor
public class TokenTestController {
    private final OAuth2AuthorizedClientManager authorizedClientManager;

    @GetMapping("/generate-token")
    public String getToken() {
        OAuth2AuthorizeRequest request = OAuth2AuthorizeRequest.withClientRegistrationId("service-client")
                .principal(new AnonymousAuthenticationToken("key", "anonymousUser",
                        AuthorityUtils.createAuthorityList("ROLE_ANONYMOUS")))
                .build();
        OAuth2AuthorizedClient client = authorizedClientManager.authorize(request);
        return client != null ? client.getAccessToken().getTokenValue() : "❌ No token";
    }
}