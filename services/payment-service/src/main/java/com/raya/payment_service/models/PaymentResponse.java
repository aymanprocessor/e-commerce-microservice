package com.raya.payment_service.models;

import java.math.BigDecimal;

public record PaymentResponse(
        String transactionId,
        String status,
        BigDecimal amount
) {}
