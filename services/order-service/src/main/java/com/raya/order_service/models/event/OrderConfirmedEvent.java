package com.raya.order_service.models.event;

public record OrderConfirmedEvent(String orderId, String transactionId) {}