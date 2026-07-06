package com.raya.order_service.models;


import java.math.BigDecimal;

public record OrderRequest(BigDecimal amount) {}
