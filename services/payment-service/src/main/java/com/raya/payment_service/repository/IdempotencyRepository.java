package com.raya.payment_service.repository;

import com.raya.payment_service.models.IdempotencyRecord;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface IdempotencyRepository extends JpaRepository<IdempotencyRecord, String> {

    @Modifying
    @Query(value = """
            INSERT INTO idempotency_records
                (idempotency_key, request_hash, response_body, status_code)
            VALUES (:key, :requestHash, NULL, 0)
            ON CONFLICT (idempotency_key) DO NOTHING
            """, nativeQuery = true)
    int claimKey(@Param("key") String key, @Param("requestHash") String requestHash);
}
