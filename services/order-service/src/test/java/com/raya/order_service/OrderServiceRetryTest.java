package com.raya.order_service;


import com.raya.order_service.models.OrderRequest;
import com.raya.order_service.models.OrderResponse;
import com.raya.order_service.models.PaymentRequest;
import com.raya.order_service.models.PaymentResponse;
import com.raya.order_service.services.OrderService;
import com.raya.order_service.services.PaymentService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@SpringBootTest
public class OrderServiceRetryTest {
    @Autowired
    private OrderService orderService;

    @MockitoBean
    private PaymentService paymentService;

    @Test
    void createOrderAsync_retriesThreeTimesBeforeSuccess() throws Exception {

        when(paymentService.processPayment(any(PaymentRequest.class)))
                .thenThrow(new RuntimeException("Attempt 1"))
                .thenThrow(new RuntimeException("Attempt 2"))
                .thenReturn(new PaymentResponse("TXN-123", new BigDecimal("100.00")));

        OrderResponse response = orderService
                .createOrderAsync(new OrderRequest(new BigDecimal("100.00")))
                .get();

        assertThat(response.status()).isEqualTo("CONFIRMED");
        assertThat(response.message()).isEqualTo("TXN-123");

        verify(paymentService, times(3))
                .processPayment(any(PaymentRequest.class));
    }
}
