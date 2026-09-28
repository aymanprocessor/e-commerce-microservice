package com.raya.product_service.repository;

import com.raya.product_service.model.Product;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface ProductRepository extends JpaRepository<Product, Long> {

    @Query("select p.id as id, p.name as name, p.price as price, p.category as categoryName " +
            "from Product p where p.id = :id")
    Optional<ProductSummaryProjection> findSummaryById(@Param("id") Long id);

    @Query("select p.id as id, p.name as name, p.price as price, p.category as categoryName from Product p")
    List<ProductSummaryProjection> findAllSummaries();
}
