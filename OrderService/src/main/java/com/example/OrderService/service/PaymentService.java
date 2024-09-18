package com.example.OrderService.service;

import org.example.PaymentRequest;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@FeignClient(name = "PaymentService")
public interface PaymentService {

    @PostMapping("/api/payments/process")
    public ResponseEntity<String> processPayment(@RequestBody PaymentRequest paymentRequest);
}
