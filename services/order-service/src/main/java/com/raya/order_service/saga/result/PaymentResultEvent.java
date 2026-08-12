package com.raya.order_service.saga.result;

public record PaymentResultEvent(String orderId, boolean success, String transactionId) {}