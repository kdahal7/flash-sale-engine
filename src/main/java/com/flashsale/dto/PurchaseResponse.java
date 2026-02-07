package com.flashsale.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PurchaseResponse {

    private boolean success;
    private String message;
    private String orderId;
    private Long productId;
    private String userId;

    public static PurchaseResponse success(String orderId, Long productId, String userId) {
        return PurchaseResponse.builder()
                .success(true)
                .message("Purchase successful! Your order has been placed.")
                .orderId(orderId)
                .productId(productId)
                .userId(userId)
                .build();
    }

    public static PurchaseResponse outOfStock(Long productId) {
        return PurchaseResponse.builder()
                .success(false)
                .message("Sorry, this product is out of stock.")
                .productId(productId)
                .build();
    }

    public static PurchaseResponse rateLimited(String userId) {
        return PurchaseResponse.builder()
                .success(false)
                .message("Rate limit exceeded. Please try again later.")
                .userId(userId)
                .build();
    }

    public static PurchaseResponse error(String message) {
        return PurchaseResponse.builder()
                .success(false)
                .message(message)
                .build();
    }
}
