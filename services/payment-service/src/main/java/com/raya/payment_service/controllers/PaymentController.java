package com.raya.payment_service.controllers;

import com.raya.payment_service.models.PaymentRequest;
import com.raya.payment_service.models.PaymentResponse;
import com.raya.payment_service.services.PaymentService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Random;
import java.util.UUID;


@RestController
@RequestMapping("/api/payments")
@RequiredArgsConstructor
public class PaymentController {
    private final PaymentService paymentService;

    @Value("${payment.delay-ms:0}")
    private long delayMs;

    @Value("${payment.failure-rate:0.5}")
    private double failureRate;

    private final Random random = new Random();

    @PostMapping
    public ResponseEntity<PaymentResponse> processPayment(@RequestBody PaymentRequest request) throws InterruptedException {

        if (delayMs > 0) {
            Thread.sleep(delayMs);
        }

        if (random.nextDouble() < failureRate) {
            throw new RuntimeException("Payment gateway timeout");
        }

        return ResponseEntity.ok(new PaymentResponse(
                UUID.randomUUID().toString(),
                "APPROVED",
                request.amount()
        ));    }
}
