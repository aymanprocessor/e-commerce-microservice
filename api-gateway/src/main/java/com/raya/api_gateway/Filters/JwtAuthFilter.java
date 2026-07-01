package com.raya.api_gateway.Filters;

import com.raya.api_gateway.security.SecurityProperties;
import com.raya.api_gateway.util.JwtUtil;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.MalformedJwtException;
import io.jsonwebtoken.security.SignatureException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.util.AntPathMatcher;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.nio.charset.StandardCharsets;
import java.util.List;

@Component
public class JwtAuthFilter implements GlobalFilter, Ordered {

    private final AntPathMatcher pathMatcher = new AntPathMatcher();

    @Autowired
    private JwtUtil jwtUtil;

    @Autowired
    private SecurityProperties securityProperties;

    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {

        ServerHttpRequest request = exchange.getRequest();
        HttpMethod method = request.getMethod();
        String path = request.getURI().getPath();

        // Step 1: Skip validation for public routes
        if (isPublicRoute(method, path)) return chain.filter(exchange);


        // Step 2: Extract Authorization header
        String authHeader = exchange.getRequest().getHeaders().getFirst(HttpHeaders.AUTHORIZATION);
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            return unauthorizedResponse(exchange, "Missing or invalid Authorization header");
        }

        // Step 3: Validate JWT
        String token = authHeader.substring(7);
        try {
            System.out.println("TOKEN = [" + token + "]");
            Claims claims = jwtUtil.validateToken(token);
            String userId = claims.getSubject();
            String role = claims.get("role", String.class);

            if (role == null || role.isBlank()) {
                return unauthorizedResponse(exchange, "Token does not contain role claim");
            }

            // Step 4: Enrich request with user info for downstream services
            ServerHttpRequest enriched = exchange.getRequest().mutate()
                    .headers(headers -> {
                        headers.remove("X-User-Id");
                        headers.remove("X-User-Role");
                        headers.add("X-User-Id", userId);
                        headers.add("X-User-Role", role);
                    }).build();

            return chain.filter(exchange.mutate().request(enriched).build());

        } catch (ExpiredJwtException e) {
            return unauthorizedResponse(exchange, "Token expired: " + e.getMessage());
        } catch (MalformedJwtException e) {
            return unauthorizedResponse(exchange, "Malformed token: " + e.getMessage());
        } catch (SignatureException e) {
            return unauthorizedResponse(exchange, "Invalid signature: " + e.getMessage());
        } catch (JwtException e) {
            return unauthorizedResponse(exchange, "Invalid token: " + e.getMessage());
        }
    }

    private boolean isPublicRoute(HttpMethod method, String path) {
        return securityProperties.getPublicRoutes().stream()
                .anyMatch(route ->
                        route.getMethod() == method &&
                                pathMatcher.match(route.getPath(), path));
    }

    private Mono<Void> unauthorizedResponse(ServerWebExchange exchange, String message) {
        ServerHttpResponse response = exchange.getResponse();
        response.setStatusCode(HttpStatus.UNAUTHORIZED);
        response.getHeaders().add(HttpHeaders.CONTENT_TYPE, "application/json");
        String body = """
                 {
                 "status": 401,
                "error": "Unauthorized",
                "message": "%s"
                 }
                """.formatted(message);
        DataBuffer buffer = response.bufferFactory().wrap(body.getBytes(StandardCharsets.UTF_8));
        return response.writeWith(Mono.just(buffer));
    }


    @Override
    public int getOrder() {
        return Ordered.HIGHEST_PRECEDENCE + 1;
    }
}
