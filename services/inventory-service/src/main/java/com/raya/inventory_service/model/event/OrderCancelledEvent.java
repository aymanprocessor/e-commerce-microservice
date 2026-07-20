package com.raya.inventory_service.model.event;

public record OrderCancelledEvent(String orderId, String reason) {}