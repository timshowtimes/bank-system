package kz.timshowtime.cashservice;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.EnableAspectJAutoProxy;

@SpringBootApplication
@EnableAspectJAutoProxy(proxyTargetClass = true)
public class CashServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(CashServiceApplication.class, args);
    }

}
