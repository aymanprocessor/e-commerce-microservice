package com.raya.payment_service.models.event;

public record InventoryReservedEvent(String orderId, String productId, int quantity) {
}