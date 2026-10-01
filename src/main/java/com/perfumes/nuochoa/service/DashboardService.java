package com.perfumes.nuochoa.service;

import java.util.List;
import java.util.Map;

public interface DashboardService {
    // === Tổng quan ===
    long countTotalUsers();
    long countActiveUsers();
    long countTotalProducts();
    long countActiveProducts();
    long countTotalOrders();
    long countOrdersByStatus(String status);
    Double getTotalRevenue();
    long countTotalBrands();
    long countTotalCategories();
    long countTotalVouchers();

    // === Thống kê theo thời gian ===
    long countNewUsersToday();
    long countNewOrdersToday();
    Double getRevenueToday();
    long countNewOrdersThisMonth();
    Double getRevenueThisMonth();

    // === Biểu đồ ===
    // Revenue last 7 days: returns list of [date_string, revenue_amount]
    List<Map<String, Object>> getRevenueLast7Days();
    // Revenue last 12 months: returns list of [month_string, revenue_amount]
    List<Map<String, Object>> getRevenueLast12Months();
    // Revenue by year (for specific year): returns list of [month, revenue]
    List<Map<String, Object>> getRevenueByYear(int year);
    // Order count by status: returns map of status -> count
    Map<String, Long> getOrderCountByStatus();
    // Product count by category: returns map of category_name -> count
    Map<String, Long> getProductCountByCategory();
    // Top selling products: returns list of [product_name, total_quantity_sold]
    List<Map<String, Object>> getTopSellingProducts(int limit);
    // Recent orders for dashboard
    List<Map<String, Object>> getRecentOrders();
}
