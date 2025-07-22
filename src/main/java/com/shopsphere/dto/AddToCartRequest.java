package com.shopsphere.dto;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

/**
 * DTO for adding or updating cartItems in the cart
 */

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AddToCartRequest {
    private String productId;
    private Integer quantity;
}