package com.raya.payment_service.saga.commands;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.raya.payment_service.exception.PaymentException;
import com.raya.payment_service.models.PaymentRequest;
import com.raya.payment_service.models.PaymentResponse;
import com.raya.payment_service.services.PaymentService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.Map;

@Service
public class PaymentSagaCommandHandler {

    private static final Logger log = LoggerFactory.getLogger(PaymentSagaCommandHandler.class);

    private final ObjectMapper objectMapper;
    private final PaymentService paymentService;
    private final KafkaTemplate<String, Object> kafkaTemplate;

    public PaymentSagaCommandHandler(ObjectMapper objectMapper,
                                     PaymentService paymentService,
                                     KafkaTemplate<String, Object> kafkaTemplate) {
        this.objectMapper = objectMapper;
        this.paymentService = paymentService;
        this.kafkaTemplate = kafkaTemplate;
    }

    @KafkaListener(topics = "saga-commands", groupId = "payment-saga-handler")
    public void handleCommand(String rawCommand) {
        try {
            @SuppressWarnings("unchecked")
            Map<String, Object> cmd = objectMapper.readValue(rawCommand, Map.class);
            String commandType = (String) cmd.getOrDefault("commandType", "");

            if (!"ProcessPaymentCommand".equals(commandType)) {
                log.debug("[PAYMENT-HANDLER] Ignoring command of type: {}", commandType);
                return;
            }

            String orderId = (String) cmd.get("orderId");
            BigDecimal amount = new BigDecimal(String.valueOf(cmd.get("amount")));
            log.info("[PAYMENT-HANDLER] Processing payment for order {}", orderId);

            try {
                // The order ID is stable across Kafka redeliveries of this payment command.
                PaymentResponse payment = paymentService.processOnce(
                        orderId, new PaymentRequest(amount));

                kafkaTemplate.send("saga-results", orderId,
                        Map.of("type", "PaymentResultEvent",
                                "orderId", orderId,
                                "success", true,
                                "transactionId", payment.transactionId()));

                log.info("[PAYMENT-HANDLER] Payment completed for order {} txId={}",
                        orderId, payment.transactionId());
            } catch (PaymentException e) {
                kafkaTemplate.send("saga-results", orderId,
                        Map.of("type", "PaymentResultEvent",
                                "orderId", orderId,
                                "success", false,
                                "transactionId", ""));

                log.warn("[PAYMENT-HANDLER] Payment failed for order {}: {}",
                        orderId, e.getMessage());
            }
        } catch (Exception e) {
            log.error("[PAYMENT-HANDLER] Failed to process command: {}", rawCommand, e);
        }
    }
}
