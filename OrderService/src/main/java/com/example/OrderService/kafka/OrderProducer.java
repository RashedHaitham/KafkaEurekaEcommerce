package com.example.OrderService.kafka;

import com.example.OrderService.entity.Order;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

import java.util.logging.Logger;

@Service
public class OrderProducer {

    private static final Logger logger = Logger.getLogger(OrderProducer.class.getName());
    private static final String TOPIC = "orders";

    private final KafkaTemplate<String, Order> kafkaTemplate;

    @Autowired
    public OrderProducer(KafkaTemplate<String, Order> kafkaTemplate) {
        this.kafkaTemplate = kafkaTemplate;
    }

    public void sendOrder(Order order) {
        logger.info("Sending order: " + order);
        kafkaTemplate.send(TOPIC, order);
    }
}
