package com.raya.inventory_service.event_handler;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.raya.inventory_service.exception.InsufficientStockException;
import com.raya.inventory_service.model.event.*;
import com.raya.inventory_service.service.InventoryService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

@Service
public class InventorySagaEventHandler {
    private static final Logger log = LoggerFactory.getLogger(InventorySagaEventHandler.class);
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Autowired
    private InventoryService inventoryService;

    @Autowired
    private KafkaTemplate<String, Object> kafkaTemplate;

    @KafkaListener(topics = "order-events", groupId = "inventory-service")
    public void handleOrderPlaced(OrderPlacedEvent event) {
        log.info("[SAGA] Handling OrderPlaced for order: {}", event.orderId());
        try {
            inventoryService.reserveStock(event.productId(), event.quantity(), event.orderId());
            // Success: publish InventoryReserved
            kafkaTemplate.send("inventory-events", event.orderId(),
                    new InventoryReservedEvent(event.orderId(), event.productId(), event.quantity()));
            log.info("[SAGA] Inventory reserved for order: {} ✅", event.orderId());
        } catch (InsufficientStockException e) {
            // Failure: publish InventoryReservationFailed (starts compensation)
            kafkaTemplate.send("inventory-events", event.orderId(),
                    new InventoryReservationFailedEvent(event.orderId(), e.getMessage()));
            log.warn("[SAGA] Inventory reservation FAILED for order: {} ✅", event.orderId());
        }
    }

    @KafkaListener(topics = "payment-events", groupId = "inventory-compensation")
    public void handlePaymentFailed(String rawEvent) {
        if (rawEvent.contains("PaymentFailed")) {
            PaymentFailedEvent event = parsePaymentFailed(rawEvent);
            inventoryService.releaseStock(event.orderId());  // undo reservation
            kafkaTemplate.send("inventory-events", event.orderId(),
                    new InventoryReleasedEvent(event.orderId()));
            log.info("[SAGA] COMPENSATION: Inventory released for order: {} ✅", event.orderId());
        }
    }

    private PaymentFailedEvent parsePaymentFailed(String rawJson) {
        try {
            return objectMapper.readValue(rawJson, PaymentFailedEvent.class);
        } catch (Exception e) {
            throw new IllegalStateException("Failed to parse PaymentFailedEvent: " + rawJson, e);
        }
    }
}
