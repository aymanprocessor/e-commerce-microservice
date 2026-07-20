package com.raya.order_service.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import com.raya.order_service.models.Order;

public interface OrderRepository extends JpaRepository<Order, String> {
}
