package com.raya.payment_service.services;

import com.raya.payment_service.exception.PaymentException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.Random;
import java.util.UUID;

@Service
public class PaymentProcessor {

    @Value("${payment.failure-rate:0.5}")
    private double failureRate;

    private final Random random = new Random();

    public String processPayment(String orderId) {
        if (random.nextDouble() < failureRate) {
            throw new PaymentException("Payment failed for order " + orderId);
        }
        return UUID.randomUUID().toString();
    }
}