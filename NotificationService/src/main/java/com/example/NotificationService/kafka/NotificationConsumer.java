package com.example.NotificationService.kafka;

import com.example.NotificationService.entity.Notification;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

import java.util.logging.Logger;

@Service
public class NotificationConsumer {

    private static final Logger logger = Logger.getLogger(NotificationConsumer.class.getName());

    @KafkaListener(topics = "notification-topic", groupId = "notificationService")
    public void consumeNotification(Notification notification) {
        logger.info("Received notification: " + notification);
        // Logic to send email/SMS or log the notification
    }
}
