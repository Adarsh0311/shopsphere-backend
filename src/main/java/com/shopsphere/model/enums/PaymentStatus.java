package com.shopsphere.model.enums;

public enum PaymentStatus {
    PENDING, COMPLETED, FAILED, REFUNDED,
    AUTHORIZED // Payment authorized (funds held), but not yet captured
}
