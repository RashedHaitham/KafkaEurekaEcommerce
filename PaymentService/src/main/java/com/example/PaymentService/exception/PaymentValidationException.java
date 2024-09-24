package com.example.PaymentService.exception;

// Custom exception for validation failures
public class PaymentValidationException extends RuntimeException {
    public PaymentValidationException(String message) {
        super(message);
    }
}



