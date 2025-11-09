package kz.timshowtime.frontendservice.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.*;

import java.math.BigDecimal;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString
@JsonIgnoreProperties(ignoreUnknown = true)
public class TransferRequest {

    private UUID transferId;

    private Long fromAccount;

    private String toAccount;

    private BigDecimal amount;

}