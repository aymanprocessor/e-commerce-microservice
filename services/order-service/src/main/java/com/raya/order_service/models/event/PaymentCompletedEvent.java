package com.raya.order_service.models.event;

public record PaymentCompletedEvent(String orderId, String transactionId) {}