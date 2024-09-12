package com.example.OrderService.kafka;

import com.example.OrderService.entity.Order;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

import java.util.logging.Logger;

@Service
public class OrderConsumer {

    private static final Logger logger = Logger.getLogger(OrderConsumer.class.getName());

    @KafkaListener(topics = "orders", groupId = "productService")
    public void consume(Order order) {
        logger.info("Received order: " + order);
        // Handle order logic here, like checking product availability
    }
}
