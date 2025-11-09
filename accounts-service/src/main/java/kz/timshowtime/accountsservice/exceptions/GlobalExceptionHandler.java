package kz.timshowtime.accountsservice.exceptions;

import org.apache.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.LocalDateTime;
import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(InsufficientBalanceException.class)
    public ResponseEntity<Map<String, Object>> handleInsufficientBalance(InsufficientBalanceException e) {
        return ResponseEntity.status(HttpStatus.SC_BAD_REQUEST).body(Map.of(
                "timestamp", LocalDateTime.now(),
                "error", "InsufficientBalance",
                "message", e.getMessage()
        ));
    }
}
