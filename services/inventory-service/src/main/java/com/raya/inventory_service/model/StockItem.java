package com.raya.inventory_service.model;

public record StockItem(
        String productId,
        int availableQty,
        int reservedQty
) {
    public boolean hasStock(int requested){
        return availableQty - reservedQty >= requested;
    }
}
