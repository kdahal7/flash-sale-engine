package com.flashsale.controller;

import com.flashsale.dto.PurchaseRequest;
import com.flashsale.dto.PurchaseResponse;
import com.flashsale.service.FlashSaleService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * Flash Sale Controller
 * Main endpoint for purchasing products
 */
@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
@Slf4j
public class FlashSaleController {

    private final FlashSaleService flashSaleService;

    /**
     * Purchase a product
     * 
     * This is THE CRITICAL ENDPOINT that handles high-concurrency purchases
     * 
     * @param productId Product ID to purchase
     * @param request Purchase request containing user ID
     * @return Purchase response with order details or error
     */
    @PostMapping("/buy/{productId}")
    public ResponseEntity<PurchaseResponse> purchase(
            @PathVariable Long productId,
            @Valid @RequestBody PurchaseRequest request) {
        
        PurchaseResponse response = flashSaleService.purchase(productId, request);
        
        HttpStatus status = response.isSuccess() ? HttpStatus.OK : HttpStatus.BAD_REQUEST;
        return ResponseEntity.status(status).body(response);
    }

    /**
     * Health check endpoint
     */
    @GetMapping("/health")
    public ResponseEntity<String> health() {
        return ResponseEntity.ok("Flash Sale Engine is running!");
    }
}
