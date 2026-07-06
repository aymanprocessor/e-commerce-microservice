package com.raya.payment_service.services;

import com.raya.payment_service.models.PaymentRequest;
import com.raya.payment_service.models.PaymentResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.Random;
import java.util.UUID;

@Service
public class PaymentService {

    @Value("${payment.failure-rate:0.5}")
    private double failureRate;

    private final Random random = new Random();

    public PaymentResponse processPayment(PaymentRequest request)
    {


        if (random.nextDouble() < failureRate) {
            throw new RuntimeException("Payment gateway timeout");
        }

        return new PaymentResponse(
                UUID.randomUUID().toString(),
                "APPROVED",
                request.amount()
        );
    }


}
