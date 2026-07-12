package com.raya.order_service.models;


import java.math.BigDecimal;

public record OrderRequest(String productId,int quantity ,String customerId,BigDecimal amount) {}
