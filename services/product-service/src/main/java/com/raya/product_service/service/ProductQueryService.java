package com.raya.product_service.service;

import com.raya.product_service.repository.ProductRepository;
import com.raya.product_service.repository.ProductSummaryProjection;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
public class ProductQueryService {
    private final ProductRepository productRepository;

    public ProductQueryService(ProductRepository productRepository) {
        this.productRepository = productRepository;
    }

    @Transactional(readOnly = true)
    @Cacheable(value = "products", key = "#id")
    public Optional<ProductSummaryProjection> findById(Long id) {
        return productRepository.findSummaryById(id);
    }

    @Transactional(readOnly = true)
    @Cacheable(value = "products", key = "'all'")
    public List<ProductSummaryProjection> findAll() {
        return productRepository.findAllSummaries();
    }
}
