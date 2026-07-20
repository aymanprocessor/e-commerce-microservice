package com.raya.payment_service.models.event;

public record PaymentFailedEvent(String orderId, String reason) {
}
