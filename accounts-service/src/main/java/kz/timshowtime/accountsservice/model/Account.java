package kz.timshowtime.accountsservice.model;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Table(name = "account")
public class Account {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false)
    private String login;

    @Column(nullable = false)
    private String password;

    private String firstName;
    private String lastName;
    private String email;
    private LocalDate birthDate;

    @Builder.Default
    private BigDecimal balance = BigDecimal.ZERO;
}
