package com.raya.payment_service.models.event;

public record OrderConfirmedEvent(String orderId, String transactionId) {}