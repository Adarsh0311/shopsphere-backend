package com.shopsphere.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AdminDashboardStats {
    private long totalOrders;
    private long totalUsers;
    private long totalProducts;
    private long totalCategories;
    private BigDecimal totalRevenue;
    private long pendingOrders;
    private long lowStockProducts;
    private List<AdminOrderSummary> recentOrders;
}
