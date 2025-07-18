package com.shopsphere.repository;

import com.shopsphere.model.Cart;
import com.shopsphere.model.CartItem;
import com.shopsphere.model.Product;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CartItemRepository extends JpaRepository<CartItem, String> {
    // Find a specific cart item within a cart for a given product
    Optional<CartItem> findByCartAndProduct(Cart cart, Product product);

    List<CartItem> findByCart(Cart cart);

}