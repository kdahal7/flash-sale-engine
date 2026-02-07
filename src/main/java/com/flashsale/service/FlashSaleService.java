package com.flashsale.service;

import com.flashsale.dto.PurchaseRequest;
import com.flashsale.dto.PurchaseResponse;
import com.flashsale.entity.Order;
import com.flashsale.entity.Product;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.UUID;

/**
 * Core Flash Sale Service
 * Orchestrates the purchase flow with rate limiting and inventory management
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class FlashSaleService {

    private final InventoryService inventoryService;
    private final RateLimitService rateLimitService;
    private final ProductService productService;
    private final OrderService orderService;

    /**
     * Process a purchase request
     * 
     * Flow:
     * 1. Rate limit check
     * 2. Atomic inventory decrement in Redis
     * 3. If successful, create order asynchronously
     * 
     * @param productId Product ID to purchase
     * @param request Purchase request with user info
     * @return Purchase response
     */
    public PurchaseResponse purchase(Long productId, PurchaseRequest request) {
        String userId = request.getUserId();
        
        log.info("Purchase attempt - Product: {}, User: {}", productId, userId);

        // Step 1: Rate limiting
        if (!rateLimitService.isAllowed(userId)) {
            log.warn("Rate limit exceeded for user: {}", userId);
            return PurchaseResponse.rateLimited(userId);
        }

        // Step 2: Validate product exists
        Product product;
        try {
            product = productService.getProduct(productId);
        } catch (Exception e) {
            log.error("Product not found: {}", productId);
            return PurchaseResponse.error("Product not found");
        }

        // Step 3: Atomic inventory decrement (THE CRITICAL PART)
        Long remainingStock = inventoryService.decrementInventory(productId);

        if (remainingStock < 0) {
            // Out of stock
            log.warn("Out of stock - Product: {}, User: {}", productId, userId);
            return PurchaseResponse.outOfStock(productId);
        }

        // Step 4: Create order asynchronously
        String orderId = UUID.randomUUID().toString();
        Order order = Order.builder()
                .orderId(orderId)
                .productId(productId)
                .userId(userId)
                .quantity(request.getQuantity())
                .price(product.getPrice())
                .status(Order.OrderStatus.CONFIRMED)
                .build();

        // Save to database asynchronously (non-blocking)
        orderService.saveOrderAsync(order);

        log.info("Purchase successful - Order: {}, Product: {}, User: {}, Remaining: {}", 
                orderId, productId, userId, remainingStock);

        return PurchaseResponse.success(orderId, productId, userId);
    }
}
