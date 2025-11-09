package kz.timshowtime.notificationsservice.controller;

import kz.timshowtime.notificationsservice.dto.NotificationRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequestMapping("/api/v1/notifications")
public class NotificationController {

    @PostMapping
    public ResponseEntity<Void> sendNotification(@RequestBody NotificationRequest request) {
        log.info("Notification received: {}", request);
        return ResponseEntity.ok().build();
    }

}
