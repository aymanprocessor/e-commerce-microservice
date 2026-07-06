package com.raya.payment_service.models;

import java.math.BigDecimal;

public record PaymentRequest(BigDecimal amount) {}
