package com.raya.product_service;

import com.raya.product_service.event.ProductChangedEvent;
import com.raya.product_service.exception.InvalidProductException;
import com.raya.product_service.model.Product;
import com.raya.product_service.repository.ProductRepository;
import com.raya.product_service.service.ProductCommandService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ProductCommandServiceTests {

    @Mock
    private ProductRepository productRepository;

    @Mock
    private ApplicationEventPublisher eventPublisher;

    @InjectMocks
    private ProductCommandService productCommandService;

    @Test
    void create_rejectsNonPositivePrice() {
        Product invalidProduct = new Product(null, "Bad", "Invalid price",
                new BigDecimal("-1.00"), "Electronics");

        assertThrows(InvalidProductException.class,
                () -> productCommandService.create(invalidProduct));

        verifyNoInteractions(productRepository, eventPublisher);
    }

    @Test
    void create_publishesProductChangedEventAfterSaving() {
        Product request = new Product(null, "Laptop", "15-inch laptop",
                new BigDecimal("999.99"), "Electronics");
        Product saved = new Product(42L, "Laptop", "15-inch laptop",
                new BigDecimal("999.99"), "Electronics");
        when(productRepository.save(request)).thenReturn(saved);

        Product result = productCommandService.create(request);

        assertThat(result).isSameAs(saved);
        ArgumentCaptor<ProductChangedEvent> eventCaptor =
                ArgumentCaptor.forClass(ProductChangedEvent.class);
        verify(eventPublisher).publishEvent(eventCaptor.capture());
        assertThat(eventCaptor.getValue().productId()).isEqualTo(42L);
        assertThat(eventCaptor.getValue().changeType()).isEqualTo("CREATED");
    }
}
