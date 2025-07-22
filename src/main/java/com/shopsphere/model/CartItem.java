package com.shopsphere.model;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "cart_items")
@Data
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class CartItem {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "cart_item_id", updatable = false, nullable = false)
    @EqualsAndHashCode.Include
    private String cartItemId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "cart_id", nullable = false)
    private Cart cart;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id", nullable = false)
    private Product product;

    @Column(name = "quantity", nullable = false)
    private Integer quantity;

    @Column(name = "price_at_addition", nullable = false) // Price when added to cart
    private BigDecimal priceAtAddition;

    @Column(name = "added_at", nullable = false, updatable = false)
    private LocalDateTime addedAt;

    @PrePersist
    protected void onCreate() {
        this.addedAt = LocalDateTime.now();
    }
}