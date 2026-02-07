package com.flashsale.service;

import com.flashsale.entity.Product;
import com.flashsale.repository.ProductRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class InventoryServiceTest {

    @Mock
    private org.springframework.data.redis.core.RedisTemplate<String, Object> redisTemplate;

    @Mock
    private org.springframework.data.redis.core.ValueOperations<String, Object> valueOperations;

    @InjectMocks
    private InventoryService inventoryService;

    @BeforeEach
    void setUp() {
        when(redisTemplate.opsForValue()).thenReturn(valueOperations);
    }

    @Test
    void testInitializeInventory() {
        // Arrange
        Long productId = 1L;
        Integer stockCount = 100;

        // Act
        inventoryService.initializeInventory(productId, stockCount);

        // Assert
        verify(valueOperations).set("product:1:stock", stockCount);
    }

    @Test
    void testDecrementInventory_Success() {
        // Arrange
        Long productId = 1L;
        when(valueOperations.decrement(anyString())).thenReturn(99L);

        // Act
        Long remaining = inventoryService.decrementInventory(productId);

        // Assert
        assertEquals(99L, remaining);
        verify(valueOperations).decrement("product:1:stock");
    }

    @Test
    void testDecrementInventory_OutOfStock() {
        // Arrange
        Long productId = 1L;
        when(valueOperations.decrement(anyString())).thenReturn(-1L);

        // Act
        Long remaining = inventoryService.decrementInventory(productId);

        // Assert
        assertEquals(-1L, remaining);
        verify(valueOperations).increment("product:1:stock"); // Rollback
    }

    @Test
    void testGetInventory() {
        // Arrange
        Long productId = 1L;
        when(valueOperations.get(anyString())).thenReturn(50);

        // Act
        Long stock = inventoryService.getInventory(productId);

        // Assert
        assertEquals(50L, stock);
    }

    @Test
    void testHasStock_True() {
        // Arrange
        Long productId = 1L;
        when(valueOperations.get(anyString())).thenReturn(10);

        // Act
        boolean hasStock = inventoryService.hasStock(productId);

        // Assert
        assertTrue(hasStock);
    }

    @Test
    void testHasStock_False() {
        // Arrange
        Long productId = 1L;
        when(valueOperations.get(anyString())).thenReturn(0);

        // Act
        boolean hasStock = inventoryService.hasStock(productId);

        // Assert
        assertFalse(hasStock);
    }
}
