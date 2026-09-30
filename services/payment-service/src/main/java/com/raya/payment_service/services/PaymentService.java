package com.raya.payment_service.services;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.raya.payment_service.models.IdempotencyRecord;
import com.raya.payment_service.models.PaymentRequest;
import com.raya.payment_service.models.PaymentResponse;
import com.raya.payment_service.repository.IdempotencyRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HexFormat;

@Service
public class PaymentService {

    private final IdempotencyRepository idempotencyRepository;
    private final PaymentProcessor paymentProcessor;
    private final ObjectMapper objectMapper;

    public PaymentService(IdempotencyRepository idempotencyRepository,
                          PaymentProcessor paymentProcessor,
                          ObjectMapper objectMapper) {
        this.idempotencyRepository = idempotencyRepository;
        this.paymentProcessor = paymentProcessor;
        this.objectMapper = objectMapper;
    }

    @Transactional
    public PaymentResponse processOnce(String key, PaymentRequest request) {
        if (key == null || key.isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "Idempotency-Key must not be blank");
        }
        if (request == null || request.amount() == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "Payment amount is required");
        }

        String requestHash = hashRequest(request);
        if (idempotencyRepository.claimKey(key, requestHash) == 0) {
            IdempotencyRecord existing = idempotencyRepository.findById(key)
                    .orElseThrow(() -> new IllegalStateException(
                            "Idempotency key conflict without a stored record: " + key));

            if (!existing.getRequestHash().equals(requestHash)) {
                throw new ResponseStatusException(HttpStatus.CONFLICT,
                        "Idempotency-Key was already used for a different payment request");
            }
            if (existing.getResponseBody() == null) {
                throw new ResponseStatusException(HttpStatus.CONFLICT,
                        "Payment for this Idempotency-Key is still processing");
            }
            return readResponse(existing.getResponseBody());
        }

        IdempotencyRecord record = new IdempotencyRecord(key, requestHash);
        PaymentResponse response = new PaymentResponse(
                paymentProcessor.processPayment(key), "APPROVED", request.amount());
        record.complete(writeResponse(response), HttpStatus.OK.value());
        idempotencyRepository.save(record);
        return response;
    }

    private String hashRequest(PaymentRequest request) {
        try {
            byte[] digest = MessageDigest.getInstance("SHA-256")
                    .digest(request.amount().stripTrailingZeros().toPlainString()
                            .getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(digest);
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("SHA-256 is unavailable", e);
        }
    }

    private String writeResponse(PaymentResponse response) {
        try {
            return objectMapper.writeValueAsString(response);
        } catch (JsonProcessingException e) {
            throw new IllegalStateException("Could not store payment response", e);
        }
    }

    private PaymentResponse readResponse(String responseBody) {
        try {
            return objectMapper.readValue(responseBody, PaymentResponse.class);
        } catch (JsonProcessingException e) {
            throw new IllegalStateException("Could not read stored payment response", e);
        }
    }
}
