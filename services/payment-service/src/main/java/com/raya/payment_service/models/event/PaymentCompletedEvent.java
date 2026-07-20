package com.raya.payment_service.models.event;

public record PaymentCompletedEvent(String orderId, String transactionId) {}