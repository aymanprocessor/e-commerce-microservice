package com.raya.order_service.event_handler;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.raya.order_service.exception.OrderNotFoundException;
import com.raya.order_service.models.Order;
import com.raya.order_service.models.OrderStatus;
import com.raya.order_service.models.event.InventoryReleasedEvent;
import com.raya.order_service.models.event.PaymentCompletedEvent;
import com.raya.order_service.models.event.PaymentFailedEvent;
import com.raya.order_service.repository.OrderRepository;
import com.raya.order_service.services.OrderService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Service;

@Service
public class OrderSagaEventHandler {

    private static final Logger log = LoggerFactory.getLogger(OrderSagaEventHandler.class);
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Autowired
    private OrderRepository orderRepository;

    @KafkaListener(topics = "payment-events", groupId = "order-service")
    public void handlePaymentEvent(
            @Payload String rawEvent,
            @Header(KafkaHeaders.RECEIVED_TOPIC) String topic) {
        if (rawEvent.contains("PaymentCompleted")) {
            PaymentCompletedEvent event = parsePaymentCompleted(rawEvent);
            Order order = orderRepository.findById(event.orderId()).orElseThrow();
            order.setStatus(OrderStatus.CONFIRMED);
            orderRepository.save(order);
            log.info("[SAGA] Order {} CONFIRMED ✅", event.orderId());
        } else if (rawEvent.contains("PaymentFailed")) {
            PaymentFailedEvent event = parsePaymentFailed(rawEvent);
            Order order = orderRepository.findById(event.orderId())
                    .orElseThrow(() -> new OrderNotFoundException("Order not found: " + event.orderId()));
            order.setStatus(OrderStatus.PAYMENT_FAILED);
            orderRepository.save(order);
            log.warn("[SAGA] Order {} payment failed, waiting for inventory release...", event.orderId());
        }


    }

    @KafkaListener(topics = "inventory-events", groupId = "order-service-cancel")
    public void handleInventoryReleased(String rawEvent) {
        if (rawEvent.contains("InventoryReleased")) {
            InventoryReleasedEvent event = parseInventoryReleased(rawEvent);
            Order order = orderRepository.findById(event.orderId()).orElseThrow();
            order.setStatus(OrderStatus.CANCELLED);
            orderRepository.save(order);
            log.info("[SAGA] Order {} CANCELLED — inventory released ✅", event.orderId());
        }
    }
    private PaymentCompletedEvent parsePaymentCompleted(String rawJson) {
        try {
            return objectMapper.readValue(rawJson, PaymentCompletedEvent.class);
        } catch (Exception e) {
            throw new IllegalStateException("Failed to parse PaymentCompletedEvent: " + rawJson, e);
        }
    }
    private InventoryReleasedEvent parseInventoryReleased(String rawJson) {
        try {
            return objectMapper.readValue(rawJson, InventoryReleasedEvent.class);
        } catch (Exception e) {
            throw new IllegalStateException("Failed to parse InventoryReleasedEvent: " + rawJson, e);
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
