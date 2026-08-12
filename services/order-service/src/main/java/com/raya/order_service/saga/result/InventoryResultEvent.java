package com.raya.order_service.saga.result;

public record InventoryResultEvent(String orderId, boolean success, String reason) {}