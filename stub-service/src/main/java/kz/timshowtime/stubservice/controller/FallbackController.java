package kz.timshowtime.stubservice.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@RestController
@RequestMapping("/api/v1/fallbacks")
@Slf4j
public class FallbackController {

    private final Map<Long, String> fallbackRequests = new ConcurrentHashMap<>();

    @PostMapping("/account")
    public ResponseEntity<String> handleAccountFallback(@RequestBody Map<String, Object> payload) {
        Long accountId = ((Number) payload.getOrDefault("accountId", 0)).longValue();
        BigDecimal amount = new BigDecimal(payload.getOrDefault("amount", "0").toString());
        String reason = (String) payload.getOrDefault("reason", "unknown");

        fallbackRequests.put(accountId, "amount=" + amount + ", reason=" + reason);
        log.warn("Received fallback for account {}: {}", accountId, reason);

        return ResponseEntity.ok("Fallback accepted for account " + accountId);
    }

    @PostMapping("/transfer")
    public ResponseEntity<String> handleTransferFallback(@RequestBody Map<String, Object> payload) {
        String transferId = (String) payload.getOrDefault("transferId", "unknown");
        log.warn("Received fallback for transfer: {}", payload);
        return ResponseEntity.ok("Fallback accepted for transfer " + transferId);
    }

    @GetMapping("/list")
    public ResponseEntity<Map<Long, String>> listFallbacks() {
        return ResponseEntity.ok(fallbackRequests);
    }
}
