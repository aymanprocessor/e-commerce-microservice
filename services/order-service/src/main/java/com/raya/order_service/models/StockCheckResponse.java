package com.raya.order_service.models;

public record StockCheckResponse(
        String productId, int requestedQuantity, boolean available, int remainingStock
) {
}
