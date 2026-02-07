package com.flashsale.service;

import com.flashsale.dto.CreateProductRequest;
import com.flashsale.entity.Product;
import com.flashsale.exception.ProductNotFoundException;
import com.flashsale.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class ProductService {

    private final ProductRepository productRepository;
    private final InventoryService inventoryService;

    /**
     * Create a new product
     */
    @Transactional
    public Product createProduct(CreateProductRequest request) {
        Product product = Product.builder()
                .name(request.getName())
                .description(request.getDescription())
                .price(request.getPrice())
                .stockCount(request.getStockCount())
                .build();

        product = productRepository.save(product);
        log.info("Created product: {} with ID: {}", product.getName(), product.getId());

        // Initialize inventory in Redis
        inventoryService.initializeInventory(product.getId(), product.getStockCount());

        return product;
    }

    /**
     * Get product by ID
     */
    public Product getProduct(Long productId) {
        return productRepository.findById(productId)
                .orElseThrow(() -> new ProductNotFoundException("Product not found: " + productId));
    }

    /**
     * Get all products
     */
    public List<Product> getAllProducts() {
        return productRepository.findAll();
    }

    /**
     * Sync product inventory from database to Redis
     */
    @Transactional(readOnly = true)
    public void syncInventoryToRedis(Long productId) {
        Product product = getProduct(productId);
        inventoryService.initializeInventory(product.getId(), product.getStockCount());
        log.info("Synced inventory to Redis for product: {}", productId);
    }

    /**
     * Update product stock in database (for reconciliation)
     */
    @Transactional
    public void updateStockInDatabase(Long productId, Integer newStock) {
        productRepository.updateStockCount(productId, newStock);
        log.info("Updated database stock for product {} to {}", productId, newStock);
    }
}
