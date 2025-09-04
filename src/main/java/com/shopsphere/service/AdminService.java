package com.shopsphere.service;

import com.shopsphere.dto.AdminDashboardStats;
import com.shopsphere.dto.AdminOrderSummary;
import com.shopsphere.model.Order;
import com.shopsphere.model.enums.OrderStatus;
import com.shopsphere.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AdminService {

    private final OrderRepository orderRepository;
    private final UserRepository userRepository;
    private final ProductRepository productRepository;
    private final CategoryRepository categoryRepository;

    public AdminDashboardStats getDashboardStats() {
        // Get basic counts
        long totalOrders = orderRepository.count();
        long totalUsers = userRepository.count();
        long totalProducts = productRepository.count();
        long totalCategories = categoryRepository.count();

        // Calculate total revenue (sum of all delivered orders)
        BigDecimal totalRevenue = orderRepository.sumTotalAmountByStatus(OrderStatus.DELIVERED);
        if (totalRevenue == null) {
            totalRevenue = BigDecimal.ZERO;
        }

        // Count pending orders
        long pendingOrders = orderRepository.countByStatus(OrderStatus.PENDING);

        // Count low stock products (assuming low stock is <= 10)
        long lowStockProducts = productRepository.findByStockQuantityLessThanEqual(10).size();

        // Get recent orders (last 10 orders)
        Pageable pageable = PageRequest.of(0, 10);
        Page<Order> recentOrdersPage = orderRepository.findAllByOrderByOrderDateDesc(pageable);
        List<AdminOrderSummary> recentOrders = recentOrdersPage.getContent()
                .stream()
                .map(this::convertToOrderSummary)
                .collect(Collectors.toList());

        return new AdminDashboardStats(
                totalOrders,
                totalUsers,
                totalProducts,
                totalCategories,
                totalRevenue,
                pendingOrders,
                lowStockProducts,
                recentOrders
        );
    }

    private AdminOrderSummary convertToOrderSummary(Order order) {
        String customerName = order.getUser().getFirstName() + " " + order.getUser().getLastName();
        int itemCount = order.getOrderItems().size();

        return new AdminOrderSummary(
                order.getOrderId(),
                customerName,
                order.getOrderDate(),
                order.getTotalAmount(),
                order.getStatus().toString(),
                itemCount
        );
    }
}
