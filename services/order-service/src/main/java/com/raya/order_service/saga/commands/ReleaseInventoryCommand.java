package com.raya.order_service.saga.commands;

public record ReleaseInventoryCommand(
        String commandType,
        String orderId,
        String productId,
        int quantity
) {
    public ReleaseInventoryCommand(String orderId, String productId, int quantity) {
        this("ReleaseInventoryCommand", orderId, productId, quantity);
    }
}
