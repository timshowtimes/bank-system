package kz.timshowtime.accountsservice.service;

import kz.timshowtime.accountsservice.model.Account;
import kz.timshowtime.accountsservice.repostitory.AccountRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class AccountService {
    private final AccountRepository accountRepository;

    public List<Account> getAll() {
        return accountRepository.findAll();
    }

    @Transactional
    public Account create(Account account) {
        return accountRepository.save(account);
    }

    public Account getById(Long id) {
        return accountRepository.findById(id).orElseThrow();
    }

    public Account update(Long id, Account updated) {
        Account existing = getById(id);
        existing.setFirstName(updated.getFirstName());
        existing.setLastName(updated.getLastName());
        existing.setEmail(updated.getEmail());
        existing.setBirthDate(updated.getBirthDate());
        return accountRepository.save(existing);
    }

    public void delete(Long id) {
        accountRepository.deleteById(id);
    }
}
