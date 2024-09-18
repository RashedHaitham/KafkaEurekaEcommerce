package com.example.OrderService.controller;

import com.example.OrderService.entity.Inventory;
import com.example.OrderService.entity.Notification;
import com.example.OrderService.service.InventoryService;
import com.example.OrderService.service.NotificationService;
import com.example.OrderService.service.PaymentService;
import org.example.PaymentRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/orders")
public class OrderController {

    private InventoryService inventoryService;
    private NotificationService notificationService;
    private PaymentService paymentService;

    @Autowired
    public OrderController( InventoryService inventoryService,
                           NotificationService notificationService, PaymentService paymentService) {
        this.inventoryService = inventoryService;
        this.notificationService = notificationService;
        this.paymentService = paymentService;
    }

    @GetMapping("/hi")
    public ResponseEntity<String> test() {
        return ResponseEntity.ok("Hello World");
    }
    @PostMapping("/placeOrder/{id}")
    public ResponseEntity<String> checkProduct(@PathVariable String id) {
        try {
            ResponseEntity<Inventory> inventoryResponse = inventoryService.checkProduct(id);

            if (inventoryResponse.getStatusCode() == HttpStatus.OK) {
                PaymentRequest paymentRequest = new PaymentRequest();
                paymentRequest.setOrderId(id);
                paymentRequest.setAmount(10);
                paymentRequest.setPaymentMethod("PAYPAL");

                ResponseEntity<String> paymentResponse = paymentService.processPayment(paymentRequest);

                if (paymentResponse.getStatusCode() == HttpStatus.OK) {
                    Notification notification = new Notification("Rashed", "Payment successful");
                    return notificationService.sendNotification(notification);
                } else {
                    return ResponseEntity.status(HttpStatus.PAYMENT_REQUIRED).body("Payment failed");
                }
            } else {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Product not found in inventory");
            }
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("An error occurred while processing the order");
        }
    }

}
