package com.raya.order_service.config;

import feign.RequestInterceptor;
import feign.RequestTemplate;
import org.springframework.security.oauth2.client.OAuth2AuthorizeRequest;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClientManager;
import org.springframework.stereotype.Component;

import java.util.Objects;

@Component
public class FeignJwtInterceptor implements RequestInterceptor {
    private final OAuth2AuthorizedClientManager authorizedClientManager;

    public FeignJwtInterceptor(OAuth2AuthorizedClientManager authorizedClientManager) {
        this.authorizedClientManager = authorizedClientManager;
    }

    @Override
    public void apply(RequestTemplate template) {
        var request = OAuth2AuthorizeRequest.withClientRegistrationId("inventory")
                .principal("order-service")
                .build();
        var client = Objects.requireNonNull(authorizedClientManager.authorize(request),
                "Could not obtain the Inventory service access token");
        template.header("Authorization", "Bearer " + client.getAccessToken().getTokenValue());
    }
}
