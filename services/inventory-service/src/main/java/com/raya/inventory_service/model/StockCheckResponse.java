package com.raya.inventory_service.model;

public record StockCheckResponse(
        String productId, int requestedQuantity, boolean available, int remainingStock
) {
}
