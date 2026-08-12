package com.raya.order_service.saga.commands;

public record ReserveInventoryCommand(
        String commandType,
        String orderId,
        String productId,
        int quantity
) {
    public ReserveInventoryCommand(String orderId, String productId, int quantity) {
        this("ReserveInventoryCommand", orderId, productId, quantity);
    }
}