package com.shopsphere.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "categories")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Category {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "category_id", updatable = false, nullable = false)
    private String categoryId;

    @Column(name = "name", nullable = false, unique = true) // Category names should be unique
    private String name;

    @Column(name = "description", columnDefinition = "TEXT")
    private String description;

    // 'mappedBy' indicates that the 'category' field in the Product entity is the owner of the relationship
    // 'cascade = CascadeType.ALL' means that if a Category is deleted, its associated Products will also be deleted.
    // @OneToMany(mappedBy = "category", cascade = CascadeType.ALL, orphanRemoval = true) // If you want to delete products when category is deleted
    @OneToMany(mappedBy = "category", fetch = FetchType.LAZY) // Products are loaded only when accessed
    private Set<Product> products = new HashSet<>(); // Initialize to prevent NullPointerException

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = LocalDateTime.now();
    }

    // Helper method to add product to category and set bidirectional relationship
    public void addProduct(Product product) {
        this.products.add(product);
        product.setCategory(this); // Set the category on the product side
    }

    // Helper method to remove product from category and unset bidirectional relationship
    public void removeProduct(Product product) {
        this.products.remove(product);
        product.setCategory(null); // Unset the category on the product side
    }
}
