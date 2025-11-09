package kz.timshowtime.accountsservice.integration;

import kz.timshowtime.accountsservice.AccountsServiceApplication;
import kz.timshowtime.accountsservice.config.TestNoSecurityConfig;
import kz.timshowtime.accountsservice.model.Account;
import kz.timshowtime.accountsservice.repostitory.AccountRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest(
        classes = {AccountsServiceApplication.class, TestNoSecurityConfig.class},
        webEnvironment = SpringBootTest.WebEnvironment.MOCK
)
@ActiveProfiles("test")
@AutoConfigureMockMvc
@TestPropertySource(properties = {
        "spring.liquibase.enabled=false",
        "spring.jpa.hibernate.ddl-auto=create-drop"
})
@Transactional
class AccountControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private AccountRepository accountRepository;

    private Account testAccount;

    @BeforeEach
    void setup() {
        testAccount = new Account();
        testAccount.setLogin("timshowtime");
        testAccount.setPassword("123456");
        testAccount.setFirstName("Timur");
        testAccount.setLastName("Sultanov");
        testAccount.setEmail("ex@gmail.com");
        testAccount.setBirthDate(LocalDate.of(1999, 8, 13));
        testAccount.setBalance(BigDecimal.valueOf(1000));
        accountRepository.save(testAccount);
    }

    @Test
    void shouldReturnAccountProfile() throws Exception {
        mockMvc.perform(get("/api/v1/accounts/me")
                        .header("X-User-Login", "timshowtime")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.login").value("timshowtime"))
                .andExpect(jsonPath("$.balance").value(1000));
    }

    @Test
    void shouldAddBalance() throws Exception {
        mockMvc.perform(post("/api/v1/accounts/{id}/add-balance", testAccount.getId())
                        .param("amount", "500"))
                .andExpect(status().isOk());

        Account updated = accountRepository.findById(testAccount.getId()).orElseThrow();
        assertThat(updated.getBalance()).isEqualByComparingTo("1500");
    }

    @Test
    void shouldRejectWithdrawIfInsufficientBalance() throws Exception {
        mockMvc.perform(post("/api/v1/accounts/{id}/reduce-balance", testAccount.getId())
                        .param("amount", "999999"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Недостаточно средств на счете"));
    }
}