package kz.timshowtime.frontendservice.controller;

import jakarta.servlet.http.HttpServletRequest;
import kz.timshowtime.frontendservice.dto.AccountProfileDto;
import kz.timshowtime.frontendservice.dto.LoginRequest;
import kz.timshowtime.frontendservice.dto.RegisterRequest;
import kz.timshowtime.frontendservice.service.FrontAuthService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.RestTemplate;

@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
public class FrontAuthController {
    private final FrontAuthService frontAuthService;
    private final RestTemplate restTemplate;


    @PostMapping("/login")
    @ResponseBody
    public ResponseEntity<?> login(@RequestBody LoginRequest reqDto,
                                   HttpServletRequest request) {
        return frontAuthService.login(reqDto, request);
    }

    @PostMapping("/register")
    public ResponseEntity<String> register(@RequestBody AccountProfileDto dto) {
        try {
            ResponseEntity<String> response = restTemplate.postForEntity(
                    "http://gateway-service/api/v1/accounts/auth/register",
                    dto,
                    String.class
            );

            return ResponseEntity.status(response.getStatusCode()).body(response.getBody());
        } catch (HttpClientErrorException | HttpServerErrorException e) {
            return ResponseEntity.status(e.getStatusCode()).body(e.getResponseBodyAsString());
        }
    }

}