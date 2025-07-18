package com.shopsphere.repository;

import com.shopsphere.model.Cart;
import com.shopsphere.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface CartRepository extends JpaRepository<Cart, String> {
    // Find a cart associated with a specific user
    Optional<Cart> findByUser(User user);
}