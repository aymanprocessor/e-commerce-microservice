package com.raya.api_gateway.config;

import com.raya.api_gateway.configs.RateLimiterConfig;
import org.junit.jupiter.api.Test;
import org.springframework.cloud.gateway.filter.ratelimit.KeyResolver;
import org.springframework.mock.http.server.reactive.MockServerHttpRequest;
import org.springframework.mock.web.server.MockServerWebExchange;

import java.net.InetSocketAddress;

import static org.junit.jupiter.api.Assertions.assertEquals;

class RateLimiterConfigTests {

    private final RateLimiterConfig config = new RateLimiterConfig();

    @Test
    void ipKeyResolver_returnsClientIp() {
        KeyResolver resolver = config.ipKeyResolver();

        MockServerHttpRequest request = MockServerHttpRequest
                .get("/api/products")
                .remoteAddress(new InetSocketAddress("127.0.0.1", 8080))
                .build();

        String key = resolver.resolve(
                MockServerWebExchange.from(request)
        ).block();

        assertEquals("127.0.0.1", key);
    }

    @Test
    void userKeyResolver_returnsUserIdHeader() {
        KeyResolver resolver = config.userKeyResolver();

        MockServerHttpRequest request = MockServerHttpRequest
                .get("/api/products")
                .header("X-User-Id", "25")
                .build();

        String key = resolver.resolve(
                MockServerWebExchange.from(request)
        ).block();

        assertEquals("25", key);
    }

    @Test
    void userKeyResolver_returnsAnonymousWhenHeaderMissing() {
        KeyResolver resolver = config.userKeyResolver();

        MockServerHttpRequest request = MockServerHttpRequest
                .get("/api/products")
                .build();

        String key = resolver.resolve(
                MockServerWebExchange.from(request)
        ).block();

        assertEquals("anonymous", key);
    }
}