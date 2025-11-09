package kz.timshowtime.transferservice.dto;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class TransferRequest {

    @NotBlank
    private UUID transferId;

    @NotNull
    @Positive
    private Long fromAccount;

    @NotNull
    @Positive
    private String toAccount;

    @NotNull
    @DecimalMin(value = "0.01")
    private BigDecimal amount;

    private Long toAccountId;
}