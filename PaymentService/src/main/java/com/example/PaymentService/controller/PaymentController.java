package com.example.PaymentService.controller;

import com.example.PaymentService.kafka.PaymentProducer;
import org.example.PaymentRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/payments")
public class PaymentController {

    private final PaymentProducer paymentProducer;

    @Autowired
    public PaymentController(PaymentProducer paymentProducer) {
        this.paymentProducer = paymentProducer;
    }

    @PostMapping("/process")
    public ResponseEntity<String> processPayment(@RequestBody PaymentRequest paymentRequest) {
        paymentProducer.sendPayment(paymentRequest);
        return ResponseEntity.ok("Payment processed successfully");
    }
}
