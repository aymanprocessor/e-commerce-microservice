package com.raya.order_service.services;

import com.raya.order_service.models.PaymentRequest;
import com.raya.order_service.models.PaymentResponse;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.Random;
import java.util.UUID;

@Service
public class PaymentService {

    private final Random random = new Random();

    private final RestTemplate restTemplate = new RestTemplate();

    private static final String PAYMENT_URL = "http://localhost:8083/api/payments";
    public PaymentResponse processPayment(PaymentRequest request) {
        return restTemplate.postForObject(PAYMENT_URL,request,PaymentResponse.class);
    }


    public PaymentResponse processPaymentMock(PaymentRequest request) {
        if (random.nextInt(10) < 5) {  // 50% chance
            throw new RuntimeException("Payment Service unavailable");
        }
        return new PaymentResponse(request.amount());
    }



}
