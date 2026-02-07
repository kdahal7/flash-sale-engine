package com.flashsale.controller;

import com.flashsale.dto.CreateProductRequest;
import com.flashsale.entity.Product;
import com.flashsale.service.InventoryService;
import com.flashsale.service.ProductService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Product Management Controller
 */
@RestController
@RequestMapping("/api/products")
@RequiredArgsConstructor
public class ProductController {

    private final ProductService productService;
    private final InventoryService inventoryService;

    /**
     * Create a new product
     */
    @PostMapping
    public ResponseEntity<Product> createProduct(@Valid @RequestBody CreateProductRequest request) {
        Product product = productService.createProduct(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(product);
    }

    /**
     * Get a product by ID
     */
    @GetMapping("/{id}")
    public ResponseEntity<Map<String, Object>> getProduct(@PathVariable Long id) {
        Product product = productService.getProduct(id);
        Long redisStock = inventoryService.getInventory(id);
        
        Map<String, Object> response = new HashMap<>();
        response.put("product", product);
        response.put("redisStock", redisStock);
        response.put("dbStock", product.getStockCount());
        
        return ResponseEntity.ok(response);
    }

    /**
     * Get all products
     */
    @GetMapping
    public ResponseEntity<List<Product>> getAllProducts() {
        List<Product> products = productService.getAllProducts();
        return ResponseEntity.ok(products);
    }

    /**
     * Sync product inventory to Redis
     * Call this after creating a product or to reset inventory
     */
    @PostMapping("/{id}/sync-redis")
    public ResponseEntity<Map<String, Object>> syncToRedis(@PathVariable Long id) {
        productService.syncInventoryToRedis(id);
        Long redisStock = inventoryService.getInventory(id);
        
        Map<String, Object> response = new HashMap<>();
        response.put("message", "Inventory synced to Redis");
        response.put("productId", id);
        response.put("redisStock", redisStock);
        
        return ResponseEntity.ok(response);
    }

    /**
     * Get current Redis inventory for a product
     */
    @GetMapping("/{id}/inventory")
    public ResponseEntity<Map<String, Object>> getInventory(@PathVariable Long id) {
        Long stock = inventoryService.getInventory(id);
        
        Map<String, Object> response = new HashMap<>();
        response.put("productId", id);
        response.put("stock", stock);
        response.put("available", stock > 0);
        
        return ResponseEntity.ok(response);
    }
}
