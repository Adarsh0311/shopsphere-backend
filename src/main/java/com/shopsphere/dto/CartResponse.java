package com.shopsphere.dto;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/**
 * DTO for displaying entire cart details
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class CartResponse {
    private String cartId;
    private String userId;
    private String username;
    private List<CartItemResponse> items; // List of cart items
    private BigDecimal totalAmount; // Sum of all itemTotals
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}