package com.raya.inventory_service.model.event;

public record PaymentFailedEvent(String orderId, String reason) {
}
