package kz.timshowtime.notificationsservice.dto;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class NotificationRequest {
    private Long fromAccount;
    private Long toAccount;
    private String message;
    private String status;
}