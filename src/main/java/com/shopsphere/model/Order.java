package com.shopsphere.model;

import com.shopsphere.model.enums.OrderStatus;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "orders")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Order {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "order_id", updatable = false, nullable = false)
    private String orderId;

    // Many-to-One relationship with User: One user can place many orders
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false) // Foreign key to User
    @ToString.Exclude // Exclude to prevent StackOverflowError
    private User user;

    @Column(name = "order_date", nullable = false, updatable = false)
    private LocalDateTime orderDate;

    @Column(name = "total_amount", nullable = false)
    private BigDecimal totalAmount;

    // Example status values: PENDING, PROCESSING, SHIPPED, DELIVERED, CANCELLED, REFUNDED
    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private OrderStatus status;


    // CascadeType.ALL and orphanRemoval are good here: if an Order is deleted, its OrderItems should also be deleted.
    @OneToMany(mappedBy = "order", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private Set<OrderItem> orderItems = new HashSet<>();


    @ManyToOne(fetch = FetchType.LAZY, cascade = CascadeType.ALL) // CascadeType.ALL to save new address with order
    @JoinColumn(name = "shipping_address_id", nullable = false)
    @ToString.Exclude
    private Address shippingAddress; // The specific shipping address used for this order


    // This indicates the payment transaction associated with this order.
    @OneToOne(mappedBy = "order", cascade = CascadeType.ALL, fetch = FetchType.LAZY, orphanRemoval = true)
    private Payment payment;

    @PrePersist
    protected void onCreate() {
        this.orderDate = LocalDateTime.now();
        // Default status for new orders
        if (this.status == null) {
            this.status = OrderStatus.PENDING;
        }
    }

    // Helper methods for bidirectional relationship management
    public void addOrderItem(OrderItem item) {
        this.orderItems.add(item);
        item.setOrder(this);
    }

    public void removeOrderItem(OrderItem item) {
        this.orderItems.remove(item);
        item.setOrder(null);
    }

    public void setPayment(Payment payment) {
        this.payment = payment;
        if (payment != null) {
            payment.setOrder(this); // Set the bidirectional link
        }
    }
}