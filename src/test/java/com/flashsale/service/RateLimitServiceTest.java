package com.flashsale.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.test.util.ReflectionTestUtils;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class RateLimitServiceTest {

    @Mock
    private RedisTemplate<String, Object> redisTemplate;

    @Mock
    private ValueOperations<String, Object> valueOperations;

    @InjectMocks
    private RateLimitService rateLimitService;

    @BeforeEach
    void setUp() {
        when(redisTemplate.opsForValue()).thenReturn(valueOperations);
        ReflectionTestUtils.setField(rateLimitService, "rateLimitEnabled", true);
        ReflectionTestUtils.setField(rateLimitService, "maxRequests", 5);
        ReflectionTestUtils.setField(rateLimitService, "windowSeconds", 1);
    }

    @Test
    void testIsAllowed_FirstRequest() {
        // Arrange
        String userId = "user123";
        when(valueOperations.get(anyString())).thenReturn(null);
        when(valueOperations.increment(anyString())).thenReturn(1L);

        // Act
        boolean allowed = rateLimitService.isAllowed(userId);

        // Assert
        assertTrue(allowed);
        verify(valueOperations).increment(anyString());
    }

    @Test
    void testIsAllowed_WithinLimit() {
        // Arrange
        String userId = "user123";
        when(valueOperations.get(anyString())).thenReturn(3);
        when(valueOperations.increment(anyString())).thenReturn(4L);

        // Act
        boolean allowed = rateLimitService.isAllowed(userId);

        // Assert
        assertTrue(allowed);
    }

    @Test
    void testIsAllowed_ExceedsLimit() {
        // Arrange
        String userId = "user123";
        when(valueOperations.get(anyString())).thenReturn(5);

        // Act
        boolean allowed = rateLimitService.isAllowed(userId);

        // Assert
        assertFalse(allowed);
        verify(valueOperations, never()).increment(anyString());
    }

    @Test
    void testIsAllowed_WhenDisabled() {
        // Arrange
        ReflectionTestUtils.setField(rateLimitService, "rateLimitEnabled", false);
        String userId = "user123";

        // Act
        boolean allowed = rateLimitService.isAllowed(userId);

        // Assert
        assertTrue(allowed);
        verify(valueOperations, never()).get(anyString());
    }

    @Test
    void testGetRemainingRequests() {
        // Arrange
        String userId = "user123";
        when(valueOperations.get(anyString())).thenReturn(3);

        // Act
        int remaining = rateLimitService.getRemainingRequests(userId);

        // Assert
        assertEquals(2, remaining);
    }

    @Test
    void testResetRateLimit() {
        // Arrange
        String userId = "user123";

        // Act
        rateLimitService.resetRateLimit(userId);

        // Assert
        verify(redisTemplate).delete(anyString());
    }
}
