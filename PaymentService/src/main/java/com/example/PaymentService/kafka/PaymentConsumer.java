package com.example.PaymentService.kafka;

import com.example.PaymentService.entity.PaymentRequest;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

import java.util.logging.Logger;

@Service
public class PaymentConsumer {

    private static final Logger logger = Logger.getLogger(PaymentConsumer.class.getName());

    @KafkaListener(topics = "payment-topic", groupId = "paymentService")
    public void consumePayment(PaymentRequest paymentRequest) {
        logger.info("Received payment confirmation for order: " + paymentRequest.getOrderId());
    }
}

