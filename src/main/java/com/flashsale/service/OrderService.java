package com.flashsale.service;

import com.flashsale.entity.Order;
import com.flashsale.repository.OrderRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.concurrent.CompletableFuture;

@Service
@RequiredArgsConstructor
@Slf4j
public class OrderService {

    private final OrderRepository orderRepository;

    /**
     * Asynchronously save order to database
     * This prevents database writes from blocking the API response
     */
    @Async("orderProcessingExecutor")
    @Transactional
    public CompletableFuture<Order> saveOrderAsync(Order order) {
        try {
            Order savedOrder = orderRepository.save(order);
            log.info("Order {} saved to database for user: {}", savedOrder.getOrderId(), savedOrder.getUserId());
            return CompletableFuture.completedFuture(savedOrder);
        } catch (Exception e) {
            log.error("Failed to save order {}", order.getOrderId(), e);
            throw e;
        }
    }

    /**
     * Get orders for a specific user
     */
    public List<Order> getUserOrders(String userId) {
        return orderRepository.findByUserId(userId);
    }

    /**
     * Get all orders
     */
    public List<Order> getAllOrders() {
        return orderRepository.findAll();
    }

    /**
     * Get order by order ID
     */
    public Order getOrder(String orderId) {
        return orderRepository.findByOrderId(orderId)
                .orElseThrow(() -> new RuntimeException("Order not found: " + orderId));
    }

    /**
     * Count confirmed orders for a product
     */
    public long countConfirmedOrders(Long productId) {
        return orderRepository.countByProductIdAndStatus(productId, Order.OrderStatus.CONFIRMED);
    }
}
