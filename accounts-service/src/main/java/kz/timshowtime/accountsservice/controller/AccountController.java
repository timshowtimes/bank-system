package kz.timshowtime.accountsservice.controller;

import kz.timshowtime.accountsservice.dto.AccountProfileDto;
import kz.timshowtime.accountsservice.dto.ChangePasswordRequest;
import kz.timshowtime.accountsservice.dto.LoginRequest;
import kz.timshowtime.accountsservice.model.Account;
import kz.timshowtime.accountsservice.service.AccountService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.Period;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/accounts")
public class AccountController {

    private final AccountService accountService;
    private final PasswordEncoder passwordEncoder;

    @GetMapping("/find/{login}")
    public ResponseEntity<?> getAccountId(@PathVariable("login") String login) {
        Account account = accountService.findByLogin(login);
        if (account == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("message", "User not found"));
        }
        return ResponseEntity.ok(account.getId());
    }

    @PostMapping("/auth/login")
    public ResponseEntity<?> login(@RequestBody LoginRequest credentials) {
        Account account = accountService.findByLogin(credentials.getLogin());
        if (account == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("message", "User not found"));
        }

        if (!passwordEncoder.matches(credentials.getPassword(), account.getPassword())) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("message", "Wrong password"));
        }

        Map<String, Object> res = Map.of(
                "id", account.getId(),
                "login", account.getLogin(),
                "first_name", account.getFirstName(),
                "last_name", account.getLastName(),
                "balance", account.getBalance()
        );

        return ResponseEntity.ok(res);
    }

    @PostMapping("/auth/register")
    public ResponseEntity<?> register(@RequestBody AccountProfileDto dto) {
        if (dto.getLogin() == null || dto.getPassword() == null ||
                dto.getFirstName() == null || dto.getLastName() == null ||
                dto.getEmail() == null || dto.getBirthDate() == null) {
            return ResponseEntity.badRequest().body(Map.of("message", "Все поля обязательны"));
        }

        if (accountService.existsByLogin(dto.getLogin())) {
            return ResponseEntity.badRequest().body(Map.of("message", "Логин уже существует"));
        }

        LocalDate birthDate = dto.getBirthDate();
        if (Period.between(birthDate, LocalDate.now()).getYears() < 18) {
            return ResponseEntity.badRequest().body(Map.of("message", "Возраст должен быть старше 18 лет"));
        }

        Account newAccount = Account.builder()
                .login(dto.getLogin())
                .password(passwordEncoder.encode(dto.getPassword()))
                .firstName(dto.getFirstName())
                .lastName(dto.getLastName())
                .email(dto.getEmail())
                .birthDate(dto.getBirthDate())
                .balance(BigDecimal.ZERO)
                .createdAt(LocalDate.now())
                .build();

        accountService.save(newAccount);

        return ResponseEntity.ok(Map.of("message", "Регистрация успешна"));
    }

    @GetMapping("/me")
    public ResponseEntity<AccountProfileDto> getProfile(@RequestHeader("X-User-Login") String login,
                                                        @AuthenticationPrincipal Jwt jwt) {
        return ResponseEntity.ok(accountService.getProfile(login));
    }

    @PostMapping("/{accountId}/change-password")
    public ResponseEntity<?> changePassword(@PathVariable("accountId") Long accountId,
                                            @RequestBody ChangePasswordRequest passwordRequest) {
        Account account = accountService.getById(accountId);

        if (account == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("message", "Аккаунт не найден"));
        }

        if (!passwordEncoder.matches(passwordRequest.getOldPassword(), account.getPassword())) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("message", "Текущий пароль неверный"));
        }

        if (passwordEncoder.matches(passwordRequest.getNewPassword(), account.getPassword())) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("message", "Новый пароль должен отличаться от старого"));
        }

        account.setPassword(passwordEncoder.encode(passwordRequest.getNewPassword()));
        accountService.save(account);
        return ResponseEntity.ok().body(Map.of("message", "Пароль успешно изменен"));
    }

    @GetMapping
    public ResponseEntity<List<Account>> getAllAccounts() {
        return ResponseEntity.ok(accountService.getAll());
    }

    @GetMapping("/{id}")
    public ResponseEntity<Account> getAccountById(@PathVariable Long id) {
        return ResponseEntity.ok(accountService.getById(id));
    }

    @PostMapping
    public ResponseEntity<Account> createAccount(@RequestBody Account account) {
        return ResponseEntity.ok(accountService.create(account));
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> updateAccount(
            @PathVariable("id") Long id,
            @RequestBody AccountProfileDto dto
    ) {
        Account account = accountService.getById(id);
        if (account == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("message", "Пользователь не найден"));
        }

        if (dto.getFirstName() == null || dto.getLastName() == null || dto.getEmail() == null || dto.getBirthDate() == null) {
            return ResponseEntity.badRequest()
                    .body(Map.of("message", "Все поля обязательны для заполнения"));
        }

        LocalDate birthDate = dto.getBirthDate();
        if (Period.between(birthDate, LocalDate.now()).getYears() < 18) {
            return ResponseEntity.badRequest()
                    .body(Map.of("message", "Возраст должен быть старше 18 лет"));
        }

        account.setFirstName(dto.getFirstName());
        account.setLastName(dto.getLastName());
        account.setEmail(dto.getEmail());
        account.setBirthDate(dto.getBirthDate());

        accountService.save(account);

        return ResponseEntity.ok(Map.of("message", "Профиль успешно обновлён"));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteAccount(@PathVariable("id") Long id) {
        Account account = accountService.getById(id);
        if (account == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("message", "Пользователь не найден"));
        }

        if (account.getBalance().compareTo(BigDecimal.ZERO) > 0) {
            return ResponseEntity.badRequest()
                    .body(Map.of("message", "Нельзя удалить аккаунт с ненулевым балансом"));
        }

        accountService.deleteAccount(account.getId());
        return ResponseEntity.ok(Map.of("message", "Аккаунт успешно удалён"));
    }

    @PostMapping("/{id}/add-balance")
    public ResponseEntity<Account> addBalance(@PathVariable("id") Long id,
                                              @RequestParam("amount") BigDecimal amount) {
        return ResponseEntity.ok(accountService.addBalance(id, amount));
    }

    @PostMapping("/{id}/reduce-balance")
    public ResponseEntity<Account> reduceBalance(@PathVariable("id") Long id,
                                                 @RequestParam("amount") BigDecimal amount) {
        return ResponseEntity.ok(accountService.reduceBalance(id, amount));
    }
}
