package com.raya.api_gateway.Filters;

import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.security.core.context.ReactiveSecurityContextHolder;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.util.Collection;
import java.util.Map;
import java.util.stream.Collectors;

@Component
public class IdentityHeaderFilter implements GlobalFilter, Ordered {
    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        // Never trust identity headers supplied by the external caller.
        ServerHttpRequest sanitized = exchange.getRequest().mutate()
                .headers(headers -> {
                    headers.remove("X-User-Id");
                    headers.remove("X-User-Role");
                })
                .build();
        ServerWebExchange cleanExchange = exchange.mutate().request(sanitized).build();

        return ReactiveSecurityContextHolder.getContext()
                .map(context -> context.getAuthentication())
                .ofType(JwtAuthenticationToken.class)
                .map(authentication -> withIdentityHeaders(cleanExchange, authentication))
                .defaultIfEmpty(cleanExchange)
                .flatMap(chain::filter);
    }

    private ServerWebExchange withIdentityHeaders(ServerWebExchange exchange,
                                                   JwtAuthenticationToken authentication) {
        var jwt = authentication.getToken();
        String roles = realmRoles(jwt.getClaims());
        ServerHttpRequest enriched = exchange.getRequest().mutate()
                .headers(headers -> {
                    headers.set("X-User-Id", jwt.getSubject());
                    if (!roles.isBlank()) {
                        headers.set("X-User-Role", roles);
                    }
                })
                .build();
        return exchange.mutate().request(enriched).build();
    }

    private String realmRoles(Map<String, Object> claims) {
        Object access = claims.get("realm_access");
        if (!(access instanceof Map<?, ?> accessMap)) return "";
        Object roles = accessMap.get("roles");
        if (!(roles instanceof Collection<?> roleCollection)) return "";
        return roleCollection.stream().map(Object::toString).collect(Collectors.joining(","));
    }

    @Override
    public int getOrder() {
        return Ordered.LOWEST_PRECEDENCE - 1;
    }
}
