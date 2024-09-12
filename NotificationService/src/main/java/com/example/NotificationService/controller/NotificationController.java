package com.example.NotificationService.controller;

import com.example.NotificationService.entity.Notification;
import com.example.NotificationService.kafka.NotificationProducer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/notifications")
public class NotificationController {

    private final NotificationProducer notificationProducer;

    @Autowired
    public NotificationController(NotificationProducer notificationProducer) {
        this.notificationProducer = notificationProducer;
    }

    @PostMapping("/send")
    public ResponseEntity<String> sendNotification(@RequestBody Notification notification) {
        notificationProducer.sendNotification(notification);
        return ResponseEntity.ok("Notification sent successfully");
    }
}
