package kz.timshowtime.cashservice.service;

import java.math.BigDecimal;

public interface NotificationClient {
    void notifySuccess(Long fromAccount, String type,
                       BigDecimal amount);


    void notifyFailure(Long fromAccount,
                       BigDecimal amount,
                       String status);
}