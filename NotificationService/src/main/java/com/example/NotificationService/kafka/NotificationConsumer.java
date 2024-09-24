package com.example.NotificationService.kafka;

import org.example.PaymentRequest;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

import java.util.logging.Logger;

@Service
public class NotificationConsumer {

    private static final Logger logger = Logger.getLogger(NotificationConsumer.class.getName());

    // Simulated notification sending method
    private void sendNotificationToUser(String message) {
        // Simulate sending a notification (e.g., email, SMS, in-app notification)
        logger.info("Sending notification to user: " + message);
    }

    @KafkaListener(topics = "payment-topic", groupId = "paymentService")
    public void consumeNotification(PaymentRequest paymentRequest) {
        logger.info("Received payment notification: " + paymentRequest);

        String paymentMethod = paymentRequest.getPaymentMethod();
        String orderId = paymentRequest.getOrderId();
        String notificationMessage = createNotificationMessage(paymentMethod, orderId);

        sendNotificationToUser(notificationMessage);
    }

    private String createNotificationMessage(String paymentMethod, String orderId) {
        return switch (paymentMethod.toUpperCase()) {
            case "PAYPAL" -> "Your order with ID " + orderId + " has been successfully paid using PayPal.";
            case "CREDIT_CARD" ->
                    "Your order with ID " + orderId + " has been successfully paid using your credit card.";
            default -> "Your order with ID " + orderId + " has been successfully paid using an unknown payment method.";
        };
    }
}
