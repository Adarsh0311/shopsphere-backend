package com.shopsphere.repository;

import com.shopsphere.model.Order;
import com.shopsphere.model.User;
import com.shopsphere.model.enums.OrderStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.List;

@Repository
public interface OrderRepository extends JpaRepository<Order, String> {
    List<Order> findByUser(User user);

    // Custom queries for admin dashboard statistics
    long countByStatus(OrderStatus status);

    @Query("SELECT SUM(o.totalAmount) FROM Order o WHERE o.status = :status")
    BigDecimal sumTotalAmountByStatus(OrderStatus status);

    Page<Order> findAllByOrderByOrderDateDesc(Pageable pageable);
}