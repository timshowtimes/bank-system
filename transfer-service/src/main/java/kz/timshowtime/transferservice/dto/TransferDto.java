package kz.timshowtime.transferservice.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.*;

import java.math.BigDecimal;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@JsonIgnoreProperties(ignoreUnknown = true)
public class TransferDto {
    private UUID transferId;
    private Long fromAccount;
    private Long toAccount;
    private BigDecimal amount;
    private String status;
    private String message;
}