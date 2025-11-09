package kz.timshowtime.transferservice.controller;

import jakarta.validation.Valid;
import kz.timshowtime.transferservice.dto.TransferDto;
import kz.timshowtime.transferservice.dto.TransferRequest;
import kz.timshowtime.transferservice.service.TransferService;
import lombok.RequiredArgsConstructor;
import org.apache.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.concurrent.CompletableFuture;

@RestController
@RequestMapping("/api/v1/transfer")
@RequiredArgsConstructor
public class TransferController {

    private final TransferService transferService;

    @PostMapping
    public CompletableFuture<ResponseEntity<?>> transfer(@RequestBody TransferRequest request) {
        return transferService.executeTransfer(request)
                .thenApply(dto -> {
                    if ("FALLBACK".equals(dto.getStatus())) {
                        return ResponseEntity.status(HttpStatus.SC_SERVICE_UNAVAILABLE).body(dto);
                    } else if (dto.getMessage() != null) {
                        return ResponseEntity.status(HttpStatus.SC_BAD_REQUEST).body(dto.getMessage());
                    }
                    return ResponseEntity.ok(dto);
                });
    }
}
