package com.raya.inventory_service.model.event;

public record PaymentCompletedEvent(String orderId, String transactionId) {}