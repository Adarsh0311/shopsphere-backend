package com.shopsphere.dto;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * DTO for returning individual cart item response in the cart view
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class CartItemResponse {
    private String cartItemId;
    private String productId;
    private String productName; // For displaying product name in cart
    private String productImageUrl; // For displaying product image
    private Integer quantity;
    private BigDecimal priceAtAddition;
    private BigDecimal itemTotal; // quantity * priceAtAddition
    private LocalDateTime addedAt;
}