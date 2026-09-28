package com.raya.product_service.controller;

import com.raya.product_service.model.Product;
import com.raya.product_service.repository.ProductSummaryProjection;
import com.raya.product_service.service.ProductCommandService;
import com.raya.product_service.service.ProductQueryService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/products")
public class ProductController {

    private final ProductCommandService productCommandService;
    private final ProductQueryService productQueryService;

    public ProductController(ProductCommandService productCommandService,
                             ProductQueryService productQueryService) {
        this.productCommandService = productCommandService;
        this.productQueryService = productQueryService;
    }

    @GetMapping
    public ResponseEntity<List<ProductSummaryProjection>> getAll() {
        return ResponseEntity.ok(productQueryService.findAll());
    }

    @GetMapping("/{id}")
    public ResponseEntity<ProductSummaryProjection> getById(@PathVariable Long id) {
        return productQueryService.findById(id)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @PostMapping
    public ResponseEntity<Product> create(@RequestBody Product product) {
        Product saved = productCommandService.create(product);
        return ResponseEntity.status(HttpStatus.CREATED).body(saved);
    }

    @PutMapping("/{id}")
    public ResponseEntity<Product> update(@PathVariable Long id, @RequestBody Product product) {
        product.setId(id);
        return ResponseEntity.ok(productCommandService.update(product));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteById(@PathVariable Long id) {
        productCommandService.deleteById(id);
        return ResponseEntity.noContent().build();
    }
}
