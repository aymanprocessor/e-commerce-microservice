package com.raya.order_service;

import com.raya.order_service.client.InventoryClient;
import com.raya.order_service.models.*;
import com.raya.order_service.services.OrderService;
import com.raya.order_service.services.PaymentService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import java.lang.reflect.Method;
import java.math.BigDecimal;
import java.util.concurrent.CompletableFuture;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class OrderServiceTest {

    @InjectMocks
    private OrderService orderService;

    @Mock
    private PaymentService paymentService;

    @Mock
    private InventoryClient inventoryClient;

    @Test
    void paymentFallback_returnsPendingOrderResponse() throws Exception {
        OrderRequest request = new OrderRequest(
                "PROD-001",
                1,
                null,
                new BigDecimal("100.00"));
        Throwable exception = new RuntimeException("Payment Service unavailable");

        CompletableFuture<OrderResponse> result = orderService.paymentFallback(request, exception);

        assertThat(result.get().status()).isEqualTo("PENDING");
    }


    @Test
    void paymentFallback_hasCorrectSignature_requestPlusThrowable() throws NoSuchMethodException {
        Method fallback = OrderService.class.getMethod("paymentFallback", OrderRequest.class, Throwable.class);

        assertThat(fallback.getParameterCount()).isEqualTo(2);
        assertThat(fallback.getParameterTypes()[0]).isEqualTo(OrderRequest.class);
        assertThat(fallback.getParameterTypes()[1]).isEqualTo(Throwable.class);
    }

    //Test 1 - Stock Available
    @Test
    void shouldCreateOrderWhenStockAvailable() throws Exception {

        OrderRequest request = new OrderRequest("P100", 2, null, new BigDecimal(500));

        when(inventoryClient.checkStock("P100", 2))
                .thenReturn(new StockCheckResponse(
                        "P100",
                        2,
                        true,
                        8
                ));
        when(paymentService.processPayment(any()))
                .thenReturn(new PaymentResponse(
                        "TXN-123",
                        "APPROVED",
                        BigDecimal.valueOf(500)
                ));
        OrderResponse response = orderService
                .createOrderAsync(request)
                .get();

        assertEquals("CONFIRMED", response.status());
        assertEquals("TXN-123", response.message());

        verify(inventoryClient).checkStock("P100", 2);
        verify(paymentService).processPayment(any());
    }

    //Test 2 - Stock Not Available
    @Test
    void shouldRejectOrderWhenStockNotAvailable() throws Exception {

        OrderRequest request = new OrderRequest("P100", 10, null, new BigDecimal(500));

        when(inventoryClient.checkStock("P100", 10))
                .thenReturn(new StockCheckResponse(
                        "P100",
                        10,
                        false,
                        3
                ));

        OrderResponse response = orderService
                .createOrderAsync(request)
                .get();

        assertEquals("REJECTED", response.status());
        assertTrue(response.message().contains("Insufficient stock"));

        verify(inventoryClient).checkStock("P100", 10);

        verify(paymentService, never())
                .processPayment(any());
    }

    //Test 3 - Inventory Service Down
    @Test
    void shouldThrowExceptionWhenInventoryServiceFails() {

        OrderRequest request = new OrderRequest("P100", 2,null ,new BigDecimal(500));

        when(inventoryClient.checkStock(anyString(), anyInt()))
                .thenThrow(new RuntimeException("Inventory Service Down"));

        assertThrows(RuntimeException.class, () ->
                orderService.createOrderAsync(request));

        verify(inventoryClient).checkStock("P100", 2);
        verify(paymentService, never()).processPayment(any());
    }
}