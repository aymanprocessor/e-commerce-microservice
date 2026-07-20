package com.raya.order_service.models.event;

public record OrderCancelledEvent(String orderId, String reason) {}