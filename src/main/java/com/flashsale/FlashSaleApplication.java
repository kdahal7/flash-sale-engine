package com.flashsale;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.scheduling.annotation.EnableAsync;

/**
 * High-Concurrency Flash Sale Engine
 * 
 * This application demonstrates:
 * - Atomic inventory management using Redis DECR
 * - Token bucket rate limiting
 * - Async order processing with queues
 * - Production-grade error handling
 * 
 * @author Flash Sale Team
 */
@SpringBootApplication
@EnableCaching
@EnableAsync
public class FlashSaleApplication {

    public static void main(String[] args) {
        SpringApplication.run(FlashSaleApplication.class, args);
    }
}
