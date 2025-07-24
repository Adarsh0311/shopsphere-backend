package com.shopsphere.dto;

import com.shopsphere.model.enums.OrderStatus;
import com.shopsphere.model.enums.PaymentStatus;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class OrderResponse {
    private String orderId;
    private String userId;
    private String username;
    private LocalDateTime orderDate;
    private BigDecimal totalAmount;
    private OrderStatus status;

    // Shipping Address details
    private String shippingStreet;
    private String shippingCity;
    private String shippingState;
    private String shippingPostalCode;
    private String shippingCountry;

    // Payment details
    private String paymentMethod;
    private String paymentTransactionId;
    private PaymentStatus paymentStatus;
    private LocalDateTime paymentDate;

    private List<OrderItemResponse> items;
}