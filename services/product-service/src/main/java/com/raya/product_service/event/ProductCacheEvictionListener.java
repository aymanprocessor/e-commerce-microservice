package com.raya.product_service.event;

import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

import java.util.Objects;

@Component
public class ProductCacheEvictionListener {
    private final CacheManager cacheManager;

    public ProductCacheEvictionListener(CacheManager cacheManager) {
        this.cacheManager = cacheManager;
    }

    @EventListener
    public void onProductChanged(ProductChangedEvent event) {
        Cache products = Objects.requireNonNull(cacheManager.getCache("products"),
                "Products cache must be configured");
        products.evict(event.productId());
        products.evict("all");
    }
}
