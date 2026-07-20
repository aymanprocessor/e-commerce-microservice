package com.raya.payment_service.models.event;

public record InventoryReservationFailedEvent(String orderId, String reason) {
}