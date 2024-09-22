package com.example.NotificationService.kafka;

import org.example.PaymentRequest;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

import java.util.logging.Logger;

@Service
public class NotificationConsumer {

    private static final Logger logger = Logger.getLogger(NotificationConsumer.class.getName());

    @KafkaListener(topics = "payment-topic", groupId = "paymentService")
    public void consumeNotification(PaymentRequest notification) {
        logger.info("Received payment notification: " + notification);
    }
}
