package com.raya.inventory_service.service;

import com.raya.inventory_service.exception.InsufficientStockException;
import com.raya.inventory_service.model.Reservation;
import com.raya.inventory_service.model.StockCheckResponse;
import com.raya.inventory_service.model.StockItem;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class InventoryService {
    private final Map<String, Reservation> reservationsByOrderId = new ConcurrentHashMap<>();

    private final Map<String, StockItem> stock = new ConcurrentHashMap<>(Map.of(
            "PROD-001", new StockItem("PROD-001", 100, 0),
            "PROD-002", new StockItem("PROD-002", 5,  0),
            "PROD-003", new StockItem("PROD-003", 0,  0)  // out of stock
    ));

    public StockCheckResponse checkStock(String productId, int requestedQty) {
        StockItem item = stock.getOrDefault(productId,
                new StockItem(productId, 0, 0));
        boolean available = item.hasStock(requestedQty);
        return new StockCheckResponse(productId, requestedQty,
                available, item.availableQty() - item.reservedQty());
    }

    public void reserveStock(String productId, int quantity, String orderId) {
        StockItem item = stock.getOrDefault(productId, new StockItem(productId, 0, 0));
        if (!item.hasStock(quantity)) {
            throw new InsufficientStockException(
                    "Cannot reserve " + quantity + " of " + productId + " for order " + orderId);
        }
        stock.put(productId, new StockItem(
                item.productId(),
                item.availableQty(),
                item.reservedQty() + quantity
        ));
        reservationsByOrderId.put(orderId, new Reservation(productId, quantity));
    }

    public void releaseStock(String orderId) {
        Reservation reservation = reservationsByOrderId.remove(orderId);
        if (reservation == null) {
            return; // already released, or nothing was ever reserved — idempotent no-op
        }
        StockItem item = stock.getOrDefault(reservation.productId(), new StockItem(reservation.productId(), 0, 0));
        int newReserved = Math.max(0, item.reservedQty() - reservation.quantity());
        stock.put(reservation.productId(), new StockItem(item.productId(), item.availableQty(), newReserved));
    }

}
