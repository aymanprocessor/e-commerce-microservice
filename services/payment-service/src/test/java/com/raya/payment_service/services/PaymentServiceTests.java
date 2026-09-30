package com.raya.payment_service.services;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.raya.payment_service.models.IdempotencyRecord;
import com.raya.payment_service.models.PaymentRequest;
import com.raya.payment_service.models.PaymentResponse;
import com.raya.payment_service.repository.IdempotencyRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PaymentServiceTests {

    @Mock
    private IdempotencyRepository idempotencyRepository;

    @Mock
    private PaymentProcessor paymentProcessor;

    private final Map<String, IdempotencyRecord> records = new HashMap<>();
    private final Set<String> claimedKeys = new HashSet<>();
    private PaymentService paymentService;

    @BeforeEach
    void setUpRepositoryBehavior() {
        paymentService = new PaymentService(
                idempotencyRepository, paymentProcessor, new ObjectMapper());

        when(idempotencyRepository.claimKey(anyString(), anyString()))
                .thenAnswer(invocation -> claimedKeys.add(invocation.getArgument(0)) ? 1 : 0);
        lenient().when(idempotencyRepository.findById(anyString()))
                .thenAnswer(invocation -> Optional.ofNullable(
                        records.get(invocation.getArgument(0))));
        doAnswer(invocation -> {
            IdempotencyRecord record = invocation.getArgument(0);
            records.put(record.getIdempotencyKey(), record);
            return record;
        }).when(idempotencyRepository).save(any(IdempotencyRecord.class));
    }

    @Test
    void retryWithSameKeyReturnsCachedResultAndChargesOnlyOnce() {
        PaymentRequest request = new PaymentRequest(new BigDecimal("25.00"));
        when(paymentProcessor.processPayment("order-123")).thenReturn("tx-123");

        PaymentResponse first = paymentService.processOnce("order-123", request);
        PaymentResponse retry = paymentService.processOnce("order-123", request);

        assertEquals(first, retry);
        assertEquals("tx-123", retry.transactionId());
        verify(paymentProcessor, times(1)).processPayment("order-123");
    }

    @Test
    void differentKeysProcessSeparatePayments() {
        PaymentRequest request = new PaymentRequest(new BigDecimal("25.00"));
        when(paymentProcessor.processPayment("order-123")).thenReturn("tx-123");
        when(paymentProcessor.processPayment("order-456")).thenReturn("tx-456");

        PaymentResponse first = paymentService.processOnce("order-123", request);
        PaymentResponse second = paymentService.processOnce("order-456", request);

        assertEquals("tx-123", first.transactionId());
        assertEquals("tx-456", second.transactionId());
        verify(paymentProcessor, times(2)).processPayment(anyString());
    }

    @Test
    void sameKeyWithDifferentAmountIsRejectedWithoutAnotherCharge() {
        when(paymentProcessor.processPayment("order-123")).thenReturn("tx-123");
        paymentService.processOnce("order-123", new PaymentRequest(new BigDecimal("25.00")));

        assertThrows(ResponseStatusException.class, () -> paymentService.processOnce(
                "order-123", new PaymentRequest(new BigDecimal("30.00"))));

        verify(paymentProcessor, times(1)).processPayment("order-123");
    }
}
