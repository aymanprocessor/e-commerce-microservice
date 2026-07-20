package com.raya.inventory_service.model.event;

public record InventoryReservationFailedEvent(String orderId, String reason) {
}