package com.raya.api_gateway.filter;

import com.raya.api_gateway.Filters.LoggingFilter;
import org.junit.jupiter.api.Test;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.core.Ordered;
import org.springframework.mock.http.server.reactive.MockServerHttpRequest;
import org.springframework.mock.web.server.MockServerWebExchange;
import reactor.core.publisher.Mono;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

public class LoggingFilterTests {

    @Test
    void shouldPassRequestToNextFilter() {

        LoggingFilter filter = new LoggingFilter();

        MockServerHttpRequest request =
                MockServerHttpRequest
                        .get("/api/products")
                        .build();

        MockServerWebExchange exchange =
                MockServerWebExchange.from(request);

        GatewayFilterChain chain =
                mock(GatewayFilterChain.class);

        when(chain.filter(exchange))
                .thenReturn(Mono.empty());

        Mono<Void> result =
                filter.filter(exchange, chain);

        result.block();


        verify(chain, times(1))
                .filter(exchange);
    }

    @Test
    void shouldHaveHighestPrecedence() {

        LoggingFilter filter = new LoggingFilter();

        assertEquals(
                Ordered.HIGHEST_PRECEDENCE,
                filter.getOrder()
        );
    }

}
