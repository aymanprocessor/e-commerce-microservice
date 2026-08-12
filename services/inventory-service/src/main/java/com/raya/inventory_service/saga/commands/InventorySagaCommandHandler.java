package com.raya.inventory_service.saga.commands;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.raya.inventory_service.exception.InsufficientStockException;
import com.raya.inventory_service.service.InventoryService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

import java.util.Map;

@Service
public class InventorySagaCommandHandler {

    private static final Logger log = LoggerFactory.getLogger(InventorySagaCommandHandler.class);

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Autowired
    private InventoryService inventoryService;

    @Autowired
    private KafkaTemplate<String, Object> kafkaTemplate;

    @KafkaListener(topics = "saga-commands", groupId = "inventory-saga-handler")
    public void handleCommand(String rawCommand) {
        try {
            @SuppressWarnings("unchecked")
            Map<String, Object> cmd = objectMapper.readValue(rawCommand, Map.class);
            String commandType = (String) cmd.getOrDefault("commandType", "");

            switch (commandType) {
                case "ReserveInventoryCommand" -> handleReserve(cmd);
                case "ReleaseInventoryCommand" -> handleRelease(cmd);
                default -> {
                    // Not our command (e.g. ProcessPaymentCommand) — ignore silently.
                    log.debug("[INVENTORY-HANDLER] Ignoring command of type: {}", commandType);
                }
            }
        } catch (Exception e) {
            log.error("[INVENTORY-HANDLER] Failed to parse command: {}", rawCommand, e);
        }
    }

    private void handleReserve(Map<String, Object> cmd) {
        String orderId   = (String) cmd.get("orderId");
        String productId = (String) cmd.get("productId");
        int quantity     = ((Number) cmd.get("quantity")).intValue();

        log.info("[INVENTORY-HANDLER] Reserving {} × {} for order {}", quantity, productId, orderId);

        try {
            inventoryService.reserveStock(productId, quantity, orderId);

            kafkaTemplate.send("saga-results", orderId,
                    Map.of("type", "InventoryResultEvent",
                            "orderId", orderId,
                            "success", true,
                            "reason", ""));

            log.info("[INVENTORY-HANDLER] Reserved {} × {} for order {}", quantity, productId, orderId);
        } catch (InsufficientStockException e) {
            kafkaTemplate.send("saga-results", orderId,
                    Map.of("type", "InventoryResultEvent",
                            "orderId", orderId,
                            "success", false,
                            "reason", e.getMessage()));

            log.warn("[INVENTORY-HANDLER] Cannot reserve for order {}: {}", orderId, e.getMessage());
        }
    }

    private void handleRelease(Map<String, Object> cmd) {
        String orderId = (String) cmd.get("orderId");

        log.info("[INVENTORY-HANDLER] Releasing reservation for order {}", orderId);

        inventoryService.releaseStock(orderId);

        kafkaTemplate.send("saga-results", orderId,
                Map.of("type", "InventoryReleasedEvent",
                        "orderId", orderId));

        log.info("[INVENTORY-HANDLER] Released reservation for order {}", orderId);
    }
}