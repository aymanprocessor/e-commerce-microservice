package com.raya.order_service.client;

import com.raya.order_service.models.StockCheckResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

@FeignClient(
        name = "INVENTORY-SERVICE", // must match Eureka service name
        path = "/api/v1/inventory"  // base path for all methods
)
public interface InventoryClient {
    @GetMapping("/check")
    StockCheckResponse checkStock(
            @RequestParam("productId") String productId,
            @RequestParam("quantity")  int quantity
    );
}