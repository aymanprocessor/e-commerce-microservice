package com.raya.order_service.saga;

import com.raya.order_service.models.Order;
import com.raya.order_service.models.OrderRequest;
import com.raya.order_service.models.OrderResponse;
import com.raya.order_service.models.OrderStatus;
import com.raya.order_service.repository.OrderRepository;
import com.raya.order_service.saga.commands.ProcessPaymentCommand;
import com.raya.order_service.saga.commands.ReleaseInventoryCommand;
import com.raya.order_service.saga.commands.ReserveInventoryCommand;
import com.raya.order_service.saga.result.InventoryReleasedEvent;
import com.raya.order_service.saga.result.InventoryResultEvent;
import com.raya.order_service.saga.result.PaymentResultEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class OrderSagaOrchestrator {

    private static final Logger log = LoggerFactory.getLogger(OrderSagaOrchestrator.class);
    private final Map<String, SagaState> sagaStates = new ConcurrentHashMap<>();
    @Autowired
    private KafkaTemplate<String, Object> kafkaTemplate;

    @Autowired
    private OrderRepository orderRepository;

    public OrderResponse startSaga(OrderRequest request) {
        String orderId = UUID.randomUUID().toString();

        Order order = new Order(
                orderId,
                request.productId(),
                request.quantity(),
                request.amount(),
                OrderStatus.PENDING
        );
        orderRepository.save(order);

        sagaStates.put(orderId, SagaState.STARTED);

        kafkaTemplate.send("saga-commands", orderId,
                new ReserveInventoryCommand(orderId, request.productId(), request.quantity()));

        transition(orderId, SagaState.INVENTORY_RESERVING);

        return new OrderResponse(orderId, "PENDING", "Order received — processing...");
    }

    // ── KafkaListener ────────────────────────────────────────────────────────────────
    @KafkaListener(topics = "saga-results", groupId = "orchestrator-inventory",
                  containerFactory = "sagaResultsListenerFactory")
    public void handleInventoryResult(InventoryResultEvent event) {
        SagaState current = sagaStates.get(event.orderId());
        if (current != SagaState.INVENTORY_RESERVING) return; // idempotency guard

        if (event.success()) {
            transition(event.orderId(), SagaState.INVENTORY_RESERVED);

            Order order = orderRepository.findById(event.orderId()).orElseThrow();
            kafkaTemplate.send("saga-commands", event.orderId(),
                    new ProcessPaymentCommand(event.orderId(), order.getAmount(), null));

            transition(event.orderId(), SagaState.PAYMENT_PROCESSING);
        } else {
            transition(event.orderId(), SagaState.INVENTORY_FAILED);
            updateOrderStatus(event.orderId(), OrderStatus.CANCELLED);
            sagaStates.remove(event.orderId());
            log.warn("[SAGA] {} inventory failed: {} — saga cancelled", event.orderId(), event.reason());
        }
    }

    @KafkaListener(topics = "saga-results", groupId = "orchestrator-payment",
            containerFactory = "sagaResultsListenerFactory")
    public void handlePaymentResult(PaymentResultEvent event) {
        SagaState current = sagaStates.get(event.orderId());
        if (current != SagaState.PAYMENT_PROCESSING) return; // idempotency guard

        if (event.success()) {
            transition(event.orderId(), SagaState.PAYMENT_COMPLETED);
            updateOrderStatus(event.orderId(), OrderStatus.CONFIRMED);
            transition(event.orderId(), SagaState.COMPLETED);
            sagaStates.remove(event.orderId());
            log.info("[SAGA] {} COMPLETED — txId: {}", event.orderId(), event.transactionId());
        } else {
            transition(event.orderId(), SagaState.PAYMENT_FAILED);

            Order order = orderRepository.findById(event.orderId()).orElseThrow();
            kafkaTemplate.send("saga-commands", event.orderId(),
                    new ReleaseInventoryCommand(event.orderId(), order.getProductId(), order.getQuantity()));

            transition(event.orderId(), SagaState.COMPENSATING);
            log.warn("[SAGA] {} payment failed — compensation triggered", event.orderId());
        }
    }


    @KafkaListener(topics = "saga-results", groupId = "orchestrator-compensation",
            containerFactory = "sagaResultsListenerFactory")
    public void handleInventoryReleased(InventoryReleasedEvent event) {

        SagaState current = sagaStates.get(event.orderId());
        if (current != SagaState.COMPENSATING) return; // idempotency guard

        transition(event.orderId(), SagaState.INVENTORY_RELEASED);
        updateOrderStatus(event.orderId(), OrderStatus.CANCELLED);
        transition(event.orderId(), SagaState.CANCELLED);
        sagaStates.remove(event.orderId());
        log.info("[SAGA] {} CANCELLED — inventory released", event.orderId());
    }

    // ── HELPERS ────────────────────────────────────────────────────────────────
    private void transition(String orderId, SagaState newState) {
        SagaState old = sagaStates.put(orderId, newState);
        log.info("[SAGA] {} {} → {}", orderId, old, newState);
    }

    private void updateOrderStatus(String orderId, OrderStatus status) {
        orderRepository.findById(orderId).ifPresent(order -> {
            order.setStatus(status);
            orderRepository.save(order);
        });
    }
    public SagaState getSagaState(String orderId) {
        return sagaStates.get(orderId);
    }

}
