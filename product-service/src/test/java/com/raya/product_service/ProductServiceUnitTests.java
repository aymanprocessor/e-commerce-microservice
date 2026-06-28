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

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ProductServiceTest {

    @Mock
    private ProductRepository productRepository;

    @InjectMocks
    private ProductService productService;

    // Test 1: findAll() returns empty list when no products exist
    @Test
    void shouldReturnEmptyListWhenNoProductsExist() {

        when(productRepository.findAll()).thenReturn(Collections.emptyList());

        List<Product> products = productService.getAllProducts();

        assertTrue(products.isEmpty());

        verify(productRepository).findAll();
    }


    // Test 2: save() stores a product and findById() retrieves it
    @Test
    void shouldSaveProductAndFindItById() {

        Product product = new Product();
        product.setId(1L);
        product.setName("Laptop");

        when(productRepository.save(any(Product.class))).thenReturn(product);
        when(productRepository.findById(1L)).thenReturn(Optional.of(product));

        Product saved = productService.createProduct(product);
        Product found = productService.getProductById(1L);

        assertNotNull(saved);
        assertEquals("Laptop", found.getName());

        verify(productRepository).save(any(Product.class));
        verify(productRepository).findById(1L);
    }


    // Test 3: findById() throws exception for non-existent id
    @Test
    void shouldThrowExceptionWhenProductDoesNotExist() {

        when(productRepository.findById(99L))
                .thenReturn(Optional.empty());

        RuntimeException exception = assertThrows(
                RuntimeException.class,
                () -> productService.getProductById(99L));

        assertEquals("product not found with id: 99", exception.getMessage());
    }

    // BONUS Test 4: deleteProduct() removes the product
    @Test
    void shouldDeleteProduct() {

        Product product = new Product();
        product.setId(1L);

        when(productRepository.findById(1L))
                .thenReturn(Optional.of(product));

        doNothing().when(productRepository).delete(product);

        productService.deleteProduct(1L);

        verify(productRepository).delete(product);
    }


    // BONUS Test 5: findAll() returns all saved products
    @Test
    void shouldReturnAllProducts() {

        Product p1 = new Product();
        p1.setId(1L);
        p1.setName("Laptop");

        Product p2 = new Product();
        p2.setId(2L);
        p2.setName("Mouse");

        when(productRepository.findAll())
                .thenReturn(List.of(p1, p2));

        List<Product> products = productService.getAllProducts();

        assertEquals(2, products.size());
    }
}