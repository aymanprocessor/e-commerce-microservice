package com.raya.order_service.models;


public record OrderResponse(
        String status,
        String message
) { }
