package com.raya.payment_service.event_handler;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.raya.payment_service.exception.PaymentException;
import com.raya.payment_service.models.event.InventoryReservedEvent;
import com.raya.payment_service.models.event.PaymentCompletedEvent;
import com.raya.payment_service.models.event.PaymentFailedEvent;
import com.raya.payment_service.services.PaymentProcessor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

@Service
public class PaymentSagaHandler {

    private static final Logger log = LoggerFactory.getLogger(PaymentSagaHandler.class);

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Autowired
    private PaymentProcessor paymentProcessor;

    @Autowired
    private KafkaTemplate<String, Object> kafkaTemplate;

    /**
     * Step 3 of the Saga: process payment once inventory is reserved.
     */
    @KafkaListener(topics = "inventory-events", groupId = "payment-service")
    public void handleInventoryReserved(String rawEvent) {
//    /    if (!rawEvent.contains("InventoryReserved")) {
//            return; // ignore InventoryReservationFailed / InventoryReleased on this topic
//        }
        InventoryReservedEvent event = parseInventoryReserved(rawEvent);
        log.info("[SAGA] Processing payment for order: {}", event.orderId());
        try {
            String txId = paymentProcessor.processPayment(event.orderId());
            kafkaTemplate.send("payment-events", event.orderId(),
                    new PaymentCompletedEvent(event.orderId(), txId));
            log.info("[SAGA] Payment COMPLETED for order: {}", event.orderId());
        } catch (PaymentException e) {
            kafkaTemplate.send("payment-events", event.orderId(),
                    new PaymentFailedEvent(event.orderId(), e.getMessage()));
            log.warn("[SAGA] Payment FAILED for order: {} — triggering compensation", event.orderId());
        }
    }

    private InventoryReservedEvent parseInventoryReserved(String rawJson) {
        try {
            return objectMapper.readValue(rawJson, InventoryReservedEvent.class);
        } catch (Exception e) {
            throw new IllegalStateException("Failed to parse InventoryReservedEvent: " + rawJson, e);
        }
    }
}