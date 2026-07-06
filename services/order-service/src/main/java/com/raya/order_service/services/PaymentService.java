package com.raya.order_service.services;

import com.raya.order_service.models.PaymentRequest;
import com.raya.order_service.models.PaymentResponse;
import org.springframework.stereotype.Service;

import java.util.Random;
import java.util.UUID;

@Service
public class PaymentService {

    private final Random random = new Random();


    public PaymentResponse processPayment(PaymentRequest request) {
        if (random.nextInt(10) < 5) {  // 50% chance
            throw new RuntimeException("Payment Service unavailable");
        }
        return new PaymentResponse(request.amount());
    }



}
