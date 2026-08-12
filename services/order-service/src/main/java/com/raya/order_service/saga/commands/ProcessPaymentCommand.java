package com.raya.order_service.saga.commands;

import java.math.BigDecimal;

public record ProcessPaymentCommand(
        String commandType,
        String orderId,
        BigDecimal amount,
        String customerId
) {
    public ProcessPaymentCommand(String orderId, BigDecimal amount, String customerId) {
        this("ProcessPaymentCommand", orderId, amount, customerId);
    }
}
