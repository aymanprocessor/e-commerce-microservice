package com.raya.order_service;

import com.raya.order_service.models.OrderRequest;
import com.raya.order_service.models.OrderResponse;
import com.raya.order_service.models.PaymentRequest;
import com.raya.order_service.models.PaymentResponse;
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
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class OrderServiceTest {

    @InjectMocks
    private OrderService orderService;

    @Mock
    private PaymentService paymentService;


    @Test
    void paymentFallback_returnsPendingOrderResponse() throws Exception {
        OrderRequest request = new OrderRequest(new BigDecimal("100.00"));
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


}