package com.raya.order_service.services;

import com.raya.order_service.models.OrderRequest;
import com.raya.order_service.models.OrderResponse;
import com.raya.order_service.models.PaymentRequest;
import com.raya.order_service.models.PaymentResponse;
import io.github.resilience4j.bulkhead.annotation.Bulkhead;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.retry.annotation.Retry;
import io.github.resilience4j.timelimiter.annotation.TimeLimiter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.concurrent.CompletableFuture;

@Service
public class OrderService {
    private static final Logger log = LoggerFactory.getLogger(OrderService.class);

    @Autowired
    private PaymentService paymentService;

    @CircuitBreaker(name = "paymentService", fallbackMethod = "paymentFallback")
    @Retry(name = "paymentService")
    public CompletableFuture<OrderResponse> createOrderAsync(OrderRequest request) {
        PaymentResponse payment = paymentService.processPayment(
                new PaymentRequest(request.amount())
        );

        return CompletableFuture.supplyAsync(() -> {
            return new OrderResponse("CONFIRMED", payment.transactionId());
        });
    }


    public CompletableFuture<OrderResponse> paymentFallback(OrderRequest request, Throwable ex) {
        log.warn("Payment failed, returning PENDING order. Reason: {}", ex.getMessage());
        return CompletableFuture.completedFuture(new OrderResponse(
                "PENDING",
                "Will retry payment"
        ));
    }
}
