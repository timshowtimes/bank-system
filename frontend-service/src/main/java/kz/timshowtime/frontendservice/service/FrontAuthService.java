package kz.timshowtime.frontendservice.service;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import kz.timshowtime.frontendservice.dto.LoginRequest;
import kz.timshowtime.frontendservice.dto.RegisterRequest;
import kz.timshowtime.frontendservice.util.KeycloakTokenManager;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class FrontAuthService {

    private final RestTemplate restTemplate;
    private final KeycloakTokenManager keycloakTokenManager;

    public ResponseEntity<?> login(LoginRequest reqDto, HttpServletRequest request) {
        try {

            String token = keycloakTokenManager.getValidAccessToken();

            UsernamePasswordAuthenticationToken authentication =
                    new UsernamePasswordAuthenticationToken(
                            reqDto.getLogin(),
                            token,
                            List.of()
                    );

            SecurityContext context = SecurityContextHolder.createEmptyContext();
            context.setAuthentication(authentication);
            SecurityContextHolder.setContext(context);

            ResponseEntity<Map> response = restTemplate.postForEntity(
                    "http://gateway-service/api/v1/accounts/auth/login",
                    reqDto, Map.class);

            if (!response.getStatusCode().is2xxSuccessful()) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Invalid credentials");
            }

            HttpSession session = request.getSession(true);
            session.setAttribute("SPRING_SECURITY_CONTEXT", context);
            session.setAttribute("login", reqDto.getLogin());
//
//            System.out.println("Token: " + token);
//
//            HttpHeaders headers = new HttpHeaders();
//            headers.add(HttpHeaders.SET_COOKIE,
//                    "ACCESS_TOKEN=" + token + "; Path=/; HttpOnly; SameSite=Lax");

            return ResponseEntity.ok()
                    .body(Map.of("message", "Login successful"));

        } catch (HttpClientErrorException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body("Invalid credentials");
        }
    }

    public ResponseEntity<?> register(RegisterRequest req) {
        try {
            ResponseEntity<Map> r = restTemplate.postForEntity(
                    "http://gateway-service/api/v1/accounts/auth/register",
                    req, Map.class);
            return ResponseEntity.ok(Map.of("message", "Registered successfully"));
        } catch (HttpClientErrorException.Conflict e) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body("User already exists");
        }
    }
}