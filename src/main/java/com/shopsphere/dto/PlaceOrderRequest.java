package com.shopsphere.dto;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

/**
 * This DTO will contain the necessary information from the user during checkout,
 * like shipping address and basic payment info
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class PlaceOrderRequest {

    private String street;
    private String city;
    private String state;
    private String postalCode;
    private String country;
    // private String savedAddressId; // If user selects a saved address

    // TODO: integrate with a payment gateway
    private String paymentMethod; // CREDIT_CARD, PAYPAL
    private String paymentMethodToken; //token from payment gateway (stripe)
}