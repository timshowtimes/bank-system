package kz.timshowtime.frontendservice.dto;

import lombok.Data;

import java.time.LocalDate;

@Data
public class RegisterRequest {
    private String login;
    private String password;
    private String firstName;
    private String lastName;
    private String email;
    private LocalDate birthDate;
}
