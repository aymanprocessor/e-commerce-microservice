package com.raya.order_service.models.event;

public record InventoryReservationFailedEvent(String orderId, String reason) {
}