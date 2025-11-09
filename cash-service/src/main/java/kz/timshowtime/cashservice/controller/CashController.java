package kz.timshowtime.cashservice.controller;

import kz.timshowtime.cashservice.service.CashService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/cash")
@RequiredArgsConstructor
@Slf4j
public class CashController {
    private final CashService cashService;

    @PostMapping("/deposit/{accountId}")
    public ResponseEntity<String> deposit(
            @PathVariable("accountId") Long accountId,
            @RequestParam("amount") BigDecimal amount) {
        return ResponseEntity.ok(cashService.deposit(accountId, amount));
    }

    @PostMapping("/withdraw/{accountId}")
    public ResponseEntity<?> withdraw(
            @PathVariable("accountId") Long accountId,
            @RequestParam("amount") BigDecimal amount) {
        String res = cashService.withdraw(accountId, amount);
        if (res.startsWith("BUSINESS_ERROR")) {
            return ResponseEntity.badRequest()
                    .body(res.replace("BUSINESS_ERROR", ""));
        }
        return ResponseEntity.ok(Map.of("message", res));
    }

}
