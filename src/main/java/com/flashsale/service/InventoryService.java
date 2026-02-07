package com.flashsale.service;

import com.flashsale.entity.Product;
import com.flashsale.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Database-based inventory management service
 * Uses atomic database operations to prevent overselling
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class InventoryService {

    private final ProductRepository productRepository;

    /**
     * Initialize inventory for a product (already handled by Product entity)
     * @param productId Product ID
     * @param stockCount Initial stock count
     */
    public void initializeInventory(Long productId, Integer stockCount) {
        productRepository.findById(productId).ifPresent(product -> {
            product.setStock(stockCount);
            productRepository.save(product);
            log.info("Initialized inventory for product {} with {} items", productId, stockCount);
        });
    }

    /**
     * Atomic decrement of inventory using database
     * Uses optimistic locking to prevent overselling
     * 
     * @param productId Product ID
     * @return remaining stock after decrement, or -1 if out of stock
     */
    @Transactional
    public Long decrementInventory(Long productId) {
        Product product = productRepository.findById(productId).orElse(null);
        
        if (product == null) {
            log.warn("Product {} not found", productId);
            return -1L;
        }
        
        // Check current stock
        if (product.getStock() <= 0) {
            log.warn("Product {} is out of stock", productId);
            return -1L;
        }
        
        // Atomic decrement using database
        product.setStock(product.getStock() - 1);
        productRepository.save(product);
        
        Long remaining = (long) product.getStock();
        log.debug("Product {} decremented. Remaining: {}", productId, remaining);
        return remaining;
    }

    /**
     * Get current inventory count
     * @param productId Product ID
     * @return current stock count
     */
    public Long getInventory(Long productId) {
        return productRepository.findById(productId)
                .map(product -> (long) product.getStock())
                .orElse(0L);
    }

    /**
     * Increment inventory (for returns/cancellations)
     * @param productId Product ID
     * @return new stock count
     */
    @Transactional
    public Long incrementInventory(Long productId) {
        Product product = productRepository.findById(productId).orElse(null);
        if (product != null) {
            product.setStock(product.getStock() + 1);
            productRepository.save(product);
            log.info("Product {} inventory incremented to {}", productId, product.getStock());
            return (long) product.getStock();
        }
        return 0L;
    }

    /**
     * Check if product has stock available
     * @param productId Product ID
     * @return true if stock > 0
     */
    public boolean hasStock(Long productId) {
        return productRepository.findById(productId)
                .map(product -> product.getStock() > 0)
                .orElse(false);
    }

    /**
     * Delete inventory (for cleanup) - not needed with database approach
     * @param productId Product ID
     */
    public void deleteInventory(Long productId) {
        log.info("Inventory cleanup for product {} (using database, no action needed)", productId);
    }
}
