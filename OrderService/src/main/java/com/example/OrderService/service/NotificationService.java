package com.example.OrderService.service;

import com.example.OrderService.entity.Notification;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;


@FeignClient(name = "NotificationService")
public interface NotificationService {

    @PostMapping("/api/notifications/send")
    public ResponseEntity<String> sendNotification(@RequestBody Notification notification);
}
