package kz.timshowtime.frontendservice.dto;

import lombok.Data;

@Data
public class TokenResponse {
    private String accessToken;
    private long expiresIn;
}