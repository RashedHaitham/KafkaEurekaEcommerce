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

    private final InventoryService inventoryService;
    private final PaymentService paymentService;

    @Autowired
    public OrderController(InventoryService inventoryService,
                           PaymentService paymentService) {
        this.inventoryService = inventoryService;
        this.paymentService = paymentService;
    }

    @GetMapping("/hi")
    public ResponseEntity<String> test() {
        return ResponseEntity.ok("Hello World");
    }

    @PostMapping("/placeOrder/{id}")
    public ResponseEntity<String> placeOrder(@PathVariable String id) {
        try {

            ResponseEntity<Inventory> inventoryResponse = inventoryService.checkProduct(id);

            if (inventoryResponse.getStatusCode() == HttpStatus.OK && inventoryResponse.getBody() != null) {

                PaymentRequest paymentRequest = new PaymentRequest();
                paymentRequest.setOrderId(id);
                paymentRequest.setAmount(10);
                paymentRequest.setPaymentMethod("PAYPAL");

                ResponseEntity<String> paymentResponse = paymentService.processPayment(paymentRequest);

                if (paymentResponse.getStatusCode() == HttpStatus.OK) {
                    return ResponseEntity.ok("Order placed successfully");
                } else {
                    return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Payment failed");
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
