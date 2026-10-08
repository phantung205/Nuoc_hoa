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


    long countNewUsersToday();
    long countNewOrdersToday();
    Double getRevenueToday();
    long countNewOrdersThisMonth();
    Double getRevenueThisMonth();


    List<Map<String, Object>> getRevenueLast7Days();
    List<Map<String, Object>> getRevenueLast12Months();
    List<Map<String, Object>> getRevenueByYear(int year);
    Map<String, Long> getOrderCountByStatus();
    Map<String, Double> getRevenueByStatus();
    Map<String, Long> getProductCountByCategory();
    List<Map<String, Object>> getTopSellingProducts(int limit);
    List<Map<String, Object>> getRecentOrders();
}
