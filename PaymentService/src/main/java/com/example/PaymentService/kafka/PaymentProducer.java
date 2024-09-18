package com.example.PaymentService.kafka;

import org.example.PaymentRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.Message;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.stereotype.Service;

import java.util.logging.Logger;

@Service
public class PaymentProducer {

    private static final Logger logger = Logger.getLogger(PaymentProducer.class.getName());
    private static final String TOPIC = "payment-topic";

    private final KafkaTemplate<String, PaymentRequest> kafkaTemplate;

    @Autowired
    public PaymentProducer(KafkaTemplate<String, PaymentRequest> kafkaTemplate) {
        this.kafkaTemplate = kafkaTemplate;
    }

    public void sendPayment(PaymentRequest paymentRequest) {
        logger.info("Sending payment request: " + paymentRequest);
        Message<PaymentRequest> message = MessageBuilder.withPayload(paymentRequest)
                .setHeader(KafkaHeaders.TOPIC, TOPIC)
                .build();
        kafkaTemplate.send(message);
    }
}
