package com.flashsale.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.concurrent.TimeUnit;

/**
 * Token Bucket Rate Limiter using Redis
 * Prevents users from spamming the purchase endpoint
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class RateLimitService {

    private final RedisTemplate<String, Object> redisTemplate;

    @Value("${flashsale.rate-limit.enabled:true}")
    private boolean rateLimitEnabled;

    @Value("${flashsale.rate-limit.max-requests:5}")
    private int maxRequests;

    @Value("${flashsale.rate-limit.window-seconds:1}")
    private int windowSeconds;

    private static final String RATE_LIMIT_KEY_PREFIX = "rate_limit:";

    /**
     * Check if user is allowed to make a request
     * Uses sliding window counter algorithm
     * 
     * @param userId User ID
     * @return true if allowed, false if rate limited
     */
    public boolean isAllowed(String userId) {
        if (!rateLimitEnabled) {
            return true;
        }

        String key = getRateLimitKey(userId);
        
        // Get current count
        Object currentObj = redisTemplate.opsForValue().get(key);
        long currentCount = currentObj != null ? Long.parseLong(currentObj.toString()) : 0;

        if (currentCount >= maxRequests) {
            log.warn("Rate limit exceeded for user: {}. Count: {}", userId, currentCount);
            return false;
        }

        // Increment counter
        Long newCount = redisTemplate.opsForValue().increment(key);
        
        if (newCount == 1) {
            // First request in window, set expiration
            redisTemplate.expire(key, windowSeconds, TimeUnit.SECONDS);
        }

        log.debug("User {} rate limit: {}/{} requests", userId, newCount, maxRequests);
        return true;
    }

    /**
     * Reset rate limit for a user (admin function)
     * @param userId User ID
     */
    public void resetRateLimit(String userId) {
        String key = getRateLimitKey(userId);
        redisTemplate.delete(key);
        log.info("Reset rate limit for user: {}", userId);
    }

    /**
     * Get remaining requests for user
     * @param userId User ID
     * @return remaining requests
     */
    public int getRemainingRequests(String userId) {
        String key = getRateLimitKey(userId);
        Object currentObj = redisTemplate.opsForValue().get(key);
        long currentCount = currentObj != null ? Long.parseLong(currentObj.toString()) : 0;
        return Math.max(0, maxRequests - (int) currentCount);
    }

    private String getRateLimitKey(String userId) {
        return RATE_LIMIT_KEY_PREFIX + userId;
    }
}
