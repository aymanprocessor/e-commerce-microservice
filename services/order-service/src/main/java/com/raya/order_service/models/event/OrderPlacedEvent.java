package com.raya.order_service.models.event;

import java.math.BigDecimal;

public record OrderPlacedEvent(
        String orderId,
        String productId,
        int quantity,
        BigDecimal amount,
        String customerId
) {}