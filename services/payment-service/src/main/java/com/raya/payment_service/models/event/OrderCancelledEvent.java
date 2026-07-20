package com.raya.payment_service.models.event;

public record OrderCancelledEvent(String orderId, String reason) {}