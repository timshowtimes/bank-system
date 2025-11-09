package kz.timshowtime.accountsservice.service;

import kz.timshowtime.accountsservice.dto.AccountProfileDto;
import kz.timshowtime.accountsservice.exceptions.InsufficientBalanceException;
import kz.timshowtime.accountsservice.model.Account;
import kz.timshowtime.accountsservice.repostitory.AccountRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class AccountService {
    private final AccountRepository accountRepository;

    public Account findByLogin(String login) {
        return accountRepository.findByLogin(login).orElse(null);
    }

    public AccountProfileDto getProfile(String login) {
        return accountRepository.findByLogin(login)
                .map(a -> AccountProfileDto.builder()
                        .id(a.getId())
                        .login(a.getLogin())
                        .firstName(a.getFirstName())
                        .lastName(a.getLastName())
                        .email(a.getEmail())
                        .birthDate(a.getBirthDate())
                        .balance(a.getBalance())
                        .build())
                .orElseThrow(() -> new UsernameNotFoundException("User not found: " + login));
    }

    public List<Account> getAll() {
        return accountRepository.findAll();
    }

    @Transactional
    public void save(Account account) {
        accountRepository.save(account);
    }

    public boolean existsByLogin(String login) {
        return accountRepository.findByLogin(login).isPresent();
    }

    @Transactional
    public Account create(Account account) {
        return accountRepository.save(account);
    }

    public Account getById(Long id) {
        return accountRepository.findById(id).orElseThrow(() -> new RuntimeException("Account not found: " + id));
    }

    public Account update(Long id, Account updated) {
        Account existing = getById(id);
        existing.setFirstName(updated.getFirstName());
        existing.setLastName(updated.getLastName());
        existing.setEmail(updated.getEmail());
        existing.setBirthDate(updated.getBirthDate());
        return accountRepository.save(existing);
    }

    @Transactional
    public Account addBalance(Long id, BigDecimal amount) {
        Account account = getById(id);
        account.setBalance(account.getBalance().add(amount));
        return accountRepository.save(account);
    }

    @Transactional
    public Account reduceBalance(Long id, BigDecimal amount) {
        Account account = getById(id);
        if (account.getBalance().compareTo(amount) < 0) {
            throw new InsufficientBalanceException("Недостаточно средств на счете");
        }
        account.setBalance(account.getBalance().subtract(amount));
        return accountRepository.save(account);
    }

    @Transactional
    public void deleteAccount(Long id) {
        accountRepository.deleteById(id);
    }
}
