package kz.timshowtime.accountsservice.service;


import kz.timshowtime.accountsservice.model.Account;
import kz.timshowtime.accountsservice.repostitory.AccountRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;

import java.math.BigDecimal;
import java.util.Optional;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(org.mockito.junit.jupiter.MockitoExtension.class)
public class AccountServiceTest {

    @Mock
    private AccountRepository accountRepository;

    @InjectMocks
    private AccountService accountService;

    private Account account;

    @BeforeEach
    void setup() {
        account = Account.builder()
                .id(1L)
                .login("tim")
                .balance(new BigDecimal("1000.00"))
                .build();
    }

    @Test
    void reduceBalanceWhenEnoughThenSubtract() {
        when(accountRepository.findById(1L)).thenReturn(Optional.of(account));
        when(accountRepository.save(any(Account.class))).thenAnswer(inv -> inv.getArgument(0));

        var result = accountService.reduceBalance(1L, new BigDecimal("200.00"));

        assertThat(result.getBalance()).isEqualByComparingTo(new BigDecimal("800.00"));
        verify(accountRepository, times(1)).save(account);
    }

    @Test
    void reduceBalanceWhenNotEnoughThenThrow() {
        when(accountRepository.findById(1L)).thenReturn(Optional.of(account));

        assertThatThrownBy(() -> accountService.reduceBalance(1L, new BigDecimal("2000.00")))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Недостаточно");

        verify(accountRepository, never()).save(any());
    }

    @Test
    void addBalanceShouldIncrease() {
        when(accountRepository.findById(1L)).thenReturn(Optional.of(account));
        when(accountRepository.save(any(Account.class))).thenAnswer(inv -> inv.getArgument(0));

        var result = accountService.addBalance(1L, new BigDecimal("50.00"));

        assertThat(result.getBalance()).isEqualByComparingTo(new BigDecimal("1050.00"));
        verify(accountRepository).save(any(Account.class));
    }

}
