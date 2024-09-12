package com.example.NotificationService.kafka;

import com.example.NotificationService.entity.Notification;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.Message;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.stereotype.Service;

import java.util.logging.Logger;

@Service
public class NotificationProducer {

    private static final Logger logger = Logger.getLogger(NotificationProducer.class.getName());
    private static final String TOPIC = "notification-topic";

    private final KafkaTemplate<String, Notification> kafkaTemplate;

    @Autowired
    public NotificationProducer(KafkaTemplate<String, Notification> kafkaTemplate) {
        this.kafkaTemplate = kafkaTemplate;
    }

    public void sendNotification(Notification notification) {
        logger.info("Sending notification: " + notification);
        Message<Notification> message = MessageBuilder.withPayload(notification)
                .setHeader(KafkaHeaders.TOPIC, TOPIC)
                .build();
        kafkaTemplate.send(message);
    }
}
