package kz.timshowtime.transferservice.service;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.math.BigDecimal;

public interface NotificationClient {
    void notifySuccess(@NotNull @Positive Long fromAccount,
                       @NotNull @Positive Long toAccount,
                       @NotNull @DecimalMin(value = "0.01") BigDecimal amount);


    void notifyFailure(@NotNull @Positive Long fromAccount,
                       @NotNull @Positive Long toAccount,
                       @NotNull @DecimalMin(value = "0.01") BigDecimal amount,
                       String status);
}
