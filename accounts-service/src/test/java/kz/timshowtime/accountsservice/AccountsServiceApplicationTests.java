package kz.timshowtime.accountsservice;

import kz.timshowtime.accountsservice.config.TestNoSecurityConfig;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;

@SpringBootTest
@ActiveProfiles("test")
@Import(TestNoSecurityConfig.class)
class AccountsServiceApplicationTests {

    @Test
    void contextLoads() {
    }

}
