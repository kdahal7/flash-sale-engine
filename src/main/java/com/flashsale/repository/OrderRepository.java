package com.flashsale.repository;

import com.flashsale.entity.Order;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface OrderRepository extends JpaRepository<Order, Long> {

    Optional<Order> findByOrderId(String orderId);

    List<Order> findByUserId(String userId);

    List<Order> findByProductId(Long productId);

    List<Order> findByStatus(Order.OrderStatus status);

    List<Order> findByCreatedAtBetween(LocalDateTime start, LocalDateTime end);

    long countByProductIdAndStatus(Long productId, Order.OrderStatus status);
}
