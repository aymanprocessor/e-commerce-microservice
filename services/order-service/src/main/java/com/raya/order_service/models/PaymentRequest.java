package com.raya.order_service.models;

import java.math.BigDecimal;

public record PaymentRequest(BigDecimal amount) {}
