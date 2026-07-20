package com.raya.inventory_service.model.event;

public record OrderConfirmedEvent(String orderId, String transactionId) {}