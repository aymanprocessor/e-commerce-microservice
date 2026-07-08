package com.raya.order_service.models;

import java.math.BigDecimal;
import java.util.UUID;

public record PaymentResponse(String transactionId, String status, BigDecimal amount) {

    public PaymentResponse(BigDecimal amount) {
        this(UUID.randomUUID().toString(),"APPROVED", amount);
    }
}
