package com.raya.api_gateway.filter;

import com.raya.api_gateway.Filters.JwtAuthFilter;
import com.raya.api_gateway.security.SecurityProperties;
import com.raya.api_gateway.util.JwtUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.core.Ordered;
import org.springframework.http.HttpMethod;
import org.springframework.mock.http.server.reactive.MockServerHttpRequest;
import org.springframework.mock.web.server.MockServerWebExchange;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class JwtFilterTests {

    @InjectMocks
    private JwtAuthFilter jwtAuthFilter;

    @Mock
    private JwtUtil jwtUtil;

    @Mock
    private SecurityProperties securityProperties;

    @Mock
    private GatewayFilterChain filterChain;





    @Test
    void getOrder_returnsHighestPrecedencePlusOne() {
        assertThat(jwtAuthFilter.getOrder()).isEqualTo(Ordered.HIGHEST_PRECEDENCE + 1);
    }

    @Test
    void filter_callsChainFilter_forPublicRoutes_withoutValidatingAnyToken() {

        SecurityProperties.PublicRoute route = new SecurityProperties.PublicRoute();
        route.setMethod(HttpMethod.GET);
        route.setPath("/api/v1/products");

        when(securityProperties.getPublicRoutes())
                .thenReturn(List.of(route));

        MockServerHttpRequest request =
                MockServerHttpRequest.get("/api/v1/products").build();

        ServerWebExchange exchange =
                MockServerWebExchange.from(request);

        when(filterChain.filter(exchange))
                .thenReturn(Mono.empty());

        StepVerifier.create(jwtAuthFilter.filter(exchange, filterChain))
                .verifyComplete();

        verify(filterChain).filter(exchange);
        verifyNoInteractions(jwtUtil);
    }
}
