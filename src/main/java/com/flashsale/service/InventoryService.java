package com.flashsale.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.concurrent.TimeUnit;

/**
 * Redis-based inventory management service
 * Uses atomic DECR operation to prevent overselling
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class InventoryService {

    private final RedisTemplate<String, Object> redisTemplate;
    
    private static final String INVENTORY_KEY_PREFIX = "product:";
    private static final String INVENTORY_KEY_SUFFIX = ":stock";

    /**
     * Initialize inventory in Redis for a product
     * @param productId Product ID
     * @param stockCount Initial stock count
     */
    public void initializeInventory(Long productId, Integer stockCount) {
        String key = getInventoryKey(productId);
        redisTemplate.opsForValue().set(key, stockCount);
        log.info("Initialized inventory for product {} with {} items", productId, stockCount);
    }

    /**
     * Atomic decrement of inventory
     * This is the CORE method that prevents overselling
     * 
     * @param productId Product ID
     * @return remaining stock after decrement, or -1 if out of stock
     */
    public Long decrementInventory(Long productId) {
        String key = getInventoryKey(productId);
        
        // Redis DECR is atomic - this is THE KEY to preventing overselling
        Long remaining = redisTemplate.opsForValue().decrement(key);
        
        if (remaining != null && remaining < 0) {
            // Stock went negative, rollback
            redisTemplate.opsForValue().increment(key);
            log.warn("Product {} is out of stock", productId);
            return -1L;
        }
        
        log.debug("Product {} decremented. Remaining: {}", productId, remaining);
        return remaining;
    }

    /**
     * Get current inventory count
     * @param productId Product ID
     * @return current stock count
     */
    public Long getInventory(Long productId) {
        String key = getInventoryKey(productId);
        Object value = redisTemplate.opsForValue().get(key);
        return value != null ? Long.valueOf(value.toString()) : 0L;
    }

    /**
     * Increment inventory (for returns/cancellations)
     * @param productId Product ID
     * @return new stock count
     */
    public Long incrementInventory(Long productId) {
        String key = getInventoryKey(productId);
        Long newValue = redisTemplate.opsForValue().increment(key);
        log.info("Product {} inventory incremented to {}", productId, newValue);
        return newValue;
    }

    /**
     * Check if product has stock available
     * @param productId Product ID
     * @return true if stock > 0
     */
    public boolean hasStock(Long productId) {
        Long stock = getInventory(productId);
        return stock != null && stock > 0;
    }

    /**
     * Delete inventory key (for cleanup)
     * @param productId Product ID
     */
    public void deleteInventory(Long productId) {
        String key = getInventoryKey(productId);
        redisTemplate.delete(key);
        log.info("Deleted inventory for product {}", productId);
    }

    private String getInventoryKey(Long productId) {
        return INVENTORY_KEY_PREFIX + productId + INVENTORY_KEY_SUFFIX;
    }
}
