package com.shopsphere.dto;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.math.BigDecimal;

/**
 * DTO for returning individual order item details.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class OrderItemResponse {
    private String orderItemId;
    private String productId;
    private String productName;
    private String productImageUrl;
    private Integer quantity;
    private BigDecimal priceAtPurchase;
    private BigDecimal itemTotal;
}