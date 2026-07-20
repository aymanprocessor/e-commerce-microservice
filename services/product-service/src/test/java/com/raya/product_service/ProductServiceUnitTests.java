package com.raya.product_service;

import com.raya.product_service.model.Product;
import com.raya.product_service.repository.ProductRepository;
import com.raya.product_service.service.ProductService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ProductServiceTest {

    @Mock
    private ProductRepository productRepository;

    @InjectMocks
    private ProductService productService;

    @Test
    void findAll_returnsEmptyList_whenNoProductsExist() {
        when(productRepository.findAll()).thenReturn(List.of());

        List<Product> products = productService.findAll();

        assertThat(products).isEmpty();
    }

    @Test
    void save_storesProduct_andFindByIdRetrievesIt() {
        Product newProduct = new Product(null, "Laptop", "15-inch laptop", new BigDecimal("999.99"), "Electronics");
        Product saved = new Product(1L, "Laptop", "15-inch laptop", new BigDecimal("999.99"), "Electronics");
        when(productRepository.save(newProduct)).thenReturn(saved);
        when(productRepository.findById(1L)).thenReturn(Optional.of(saved));

        Product result = productService.save(newProduct);
        Optional<Product> found = productService.findById(result.getId());

        assertThat(found).isPresent();
        assertThat(found.get().getName()).isEqualTo("Laptop");
        assertThat(found.get().getPrice()).isEqualTo(new BigDecimal("999.99"));
    }

    @Test
    void findById_returnsEmptyOptional_forNonExistentId() {
        when(productRepository.findById(999L)).thenReturn(Optional.empty());

        Optional<Product> found = productService.findById(999L);

        assertThat(found).isEmpty();
    }

    @Test
    void deleteById_callsRepositoryDeleteById() {
        productService.deleteById(5L);

        verify(productRepository, times(1)).deleteById(5L);
    }
}