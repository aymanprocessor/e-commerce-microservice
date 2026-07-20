package com.raya.order_service.models.event;

public record PaymentFailedEvent(String orderId, String reason) {
}
