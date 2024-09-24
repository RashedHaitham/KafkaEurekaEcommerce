package com.example.PaymentService.service;

import com.example.PaymentService.exception.PaymentGatewayException;
import com.example.PaymentService.exception.PaymentValidationException;
import org.example.PaymentRequest;
import org.springframework.stereotype.Service;

@Service
public class PaymentService {

    public void processPayment(PaymentRequest paymentRequest) {
        // Step 1: Validate the payment request
        String validationError = validatePaymentRequest(paymentRequest);
        if (validationError != null) {
            throw new PaymentValidationException(validationError);
        }

        // Step 2: Process the payment (simulate payment gateway)
        boolean paymentSuccess = simulatePaymentGateway(paymentRequest);

        // Step 3: If payment failed, throw PaymentGatewayException
        if (!paymentSuccess) {
            throw new PaymentGatewayException("Payment failed due to gateway error");
        }
    }

    private String validatePaymentRequest(PaymentRequest paymentRequest) {
        if (paymentRequest.getAmount() <= 0) {
            return "Invalid payment amount";
        }

        if (paymentRequest.getPaymentMethod() == null || paymentRequest.getPaymentMethod().isEmpty()) {
            return "Payment method is required";
        }

        if (paymentRequest.getOrderId() == null || paymentRequest.getOrderId().isEmpty()) {
            return "Order ID is required";
        }

        if (!isValidPaymentMethod(paymentRequest.getPaymentMethod())) {
            return "Unsupported payment method";
        }

        return null; // No validation errors
    }

    private boolean simulatePaymentGateway(PaymentRequest paymentRequest) {
        return paymentRequest.getAmount() <= 1000; // Simulating success if amount is <= 1000
    }

    private boolean isValidPaymentMethod(String paymentMethod) {
        return paymentMethod.equalsIgnoreCase("PAYPAL") || paymentMethod.equalsIgnoreCase("CREDIT_CARD");
    }
}
