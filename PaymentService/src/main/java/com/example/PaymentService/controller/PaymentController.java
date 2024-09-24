package com.example.PaymentService.controller;

import com.example.PaymentService.exception.PaymentGatewayException;
import com.example.PaymentService.exception.PaymentValidationException;
import com.example.PaymentService.kafka.PaymentProducer;
import com.example.PaymentService.service.PaymentService;
import org.example.PaymentRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/payments")
public class PaymentController {

    private final PaymentProducer paymentProducer;
    private final PaymentService paymentService;

    @Autowired
    public PaymentController(PaymentProducer paymentProducer, PaymentService paymentService) {
        this.paymentProducer = paymentProducer;
        this.paymentService = paymentService;
    }

    @PostMapping("/process")
    public ResponseEntity<String> processPayment(@RequestBody PaymentRequest paymentRequest) {
        try {
            // Step 1: Process the payment via PaymentService
            paymentService.processPayment(paymentRequest);

            // Step 2: If payment is successful, send the payment request to Kafka
            paymentProducer.sendPayment(paymentRequest);

            // Return success response
            return ResponseEntity.ok("Payment processed successfully");

        } catch (PaymentValidationException e) {
            // Handle validation errors
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());

        } catch (PaymentGatewayException e) {
            // Handle payment gateway errors
            return ResponseEntity.status(HttpStatus.PAYMENT_REQUIRED).body(e.getMessage());

        } catch (Exception e) {
            // Handle any other unexpected errors
            e.printStackTrace(); // Consider logging the exception in production for debugging
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("An unexpected error occurred while processing the payment.");
        }
    }

}
