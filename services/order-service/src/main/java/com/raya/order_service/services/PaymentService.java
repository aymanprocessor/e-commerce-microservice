package com.raya.order_service.services;

import com.raya.order_service.models.PaymentRequest;
import com.raya.order_service.models.PaymentResponse;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.Random;
import java.util.UUID;

@Service
public class PaymentService {

    private final Random random = new Random();

    private final RestTemplate restTemplate = new RestTemplate();

    private static final String PAYMENT_URL = "http://localhost:8083/api/v1/payments";
    public PaymentResponse processPayment(String idempotencyKey, PaymentRequest request) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("Idempotency-Key", idempotencyKey);
        ResponseEntity<PaymentResponse> response = restTemplate.exchange(
                PAYMENT_URL,
                HttpMethod.POST,
                new HttpEntity<>(request, headers),
                PaymentResponse.class);
        return response.getBody();
    }


    public PaymentResponse processPaymentMock(PaymentRequest request) {
        if (random.nextInt(10) < 5) {  // 50% chance
            throw new RuntimeException("Payment Service unavailable");
        }
        return new PaymentResponse(request.amount());
    }



}
