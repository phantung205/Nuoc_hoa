package com.perfumes.nuochoa.service.impl;

import com.perfumes.nuochoa.entity.Category;
import com.perfumes.nuochoa.entity.Order;
import com.perfumes.nuochoa.repository.BrandRepository;
import com.perfumes.nuochoa.repository.CategoryRepository;
import com.perfumes.nuochoa.repository.OrderDetailRepository;
import com.perfumes.nuochoa.repository.OrderRepository;
import com.perfumes.nuochoa.repository.ProductRepository;
import com.perfumes.nuochoa.repository.UserRepository;
import com.perfumes.nuochoa.repository.VoucherRepository;
import com.perfumes.nuochoa.service.DashboardService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class DashboardServiceImpl implements DashboardService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private OrderDetailRepository orderDetailRepository;

    @Autowired
    private CategoryRepository categoryRepository;

    @Autowired
    private BrandRepository brandRepository;

    @Autowired
    private VoucherRepository voucherRepository;

    @Override
    public long countTotalUsers() {
        return userRepository.count();
    }

    @Override
    public long countActiveUsers() {
        return userRepository.countByStatus("ACTIVE");
    }

    @Override
    public long countTotalProducts() {
        return productRepository.count();
    }

    @Override
    public long countActiveProducts() {
        return productRepository.countByIsActiveTrue();
    }

    @Override
    public long countTotalOrders() {
        return orderRepository.count();
    }

    @Override
    public long countOrdersByStatus(String status) {
        return orderRepository.countByStatus(status);
    }

    @Override
    public Double getTotalRevenue() {
        Double revenue = orderRepository.getTotalRevenue();
        return revenue != null ? revenue : 0.0;
    }

    @Override
    public long countTotalBrands() {
        return brandRepository.count();
    }

    @Override
    public long countTotalCategories() {
        return categoryRepository.count();
    }

    @Override
    public long countTotalVouchers() {
        return voucherRepository.count();
    }

    @Override
    public long countNewUsersToday() {
        LocalDateTime startOfDay = LocalDate.now().atStartOfDay();
        LocalDateTime endOfDay = LocalDate.now().atTime(LocalTime.MAX);
        return userRepository.countByCreatedAtBetween(startOfDay, endOfDay);
    }

    @Override
    public long countNewOrdersToday() {
        LocalDateTime startOfDay = LocalDate.now().atStartOfDay();
        LocalDateTime endOfDay = LocalDate.now().atTime(LocalTime.MAX);
        return orderRepository.countByCreatedAtBetween(startOfDay, endOfDay);
    }

    @Override
    public Double getRevenueToday() {
        LocalDateTime startOfDay = LocalDate.now().atStartOfDay();
        LocalDateTime endOfDay = LocalDate.now().atTime(LocalTime.MAX);
        Double revenue = orderRepository.getRevenueBetweenDates(startOfDay, endOfDay);
        return revenue != null ? revenue : 0.0;
    }

    @Override
    public long countNewOrdersThisMonth() {
        LocalDateTime startOfMonth = LocalDate.now().withDayOfMonth(1).atStartOfDay();
        LocalDateTime endOfMonth = LocalDate.now().plusMonths(1).withDayOfMonth(1).atStartOfDay().minusNanos(1);
        return orderRepository.countByCreatedAtBetween(startOfMonth, endOfMonth);
    }

    @Override
    public Double getRevenueThisMonth() {
        LocalDateTime startOfMonth = LocalDate.now().withDayOfMonth(1).atStartOfDay();
        LocalDateTime endOfMonth = LocalDate.now().plusMonths(1).withDayOfMonth(1).atStartOfDay().minusNanos(1);
        Double revenue = orderRepository.getRevenueBetweenDates(startOfMonth, endOfMonth);
        return revenue != null ? revenue : 0.0;
    }

    @Override
    public List<Map<String, Object>> getRevenueLast7Days() {
        List<Map<String, Object>> result = new ArrayList<>();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM");
        for (int i = 6; i >= 0; i--) {
            LocalDate date = LocalDate.now().minusDays(i);
            LocalDateTime startOfDay = date.atStartOfDay();
            LocalDateTime endOfDay = date.atTime(LocalTime.MAX);
            Double revenue = orderRepository.getRevenueBetweenDates(startOfDay, endOfDay);

            Map<String, Object> map = new HashMap<>();
            map.put("date", date.format(formatter));
            map.put("revenue", revenue != null ? revenue : 0.0);
            result.add(map);
        }
        return result;
    }

    @Override
    public List<Map<String, Object>> getRevenueLast12Months() {
        List<Map<String, Object>> result = new ArrayList<>();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MM/yyyy");
        for (int i = 11; i >= 0; i--) {
            LocalDate month = LocalDate.now().minusMonths(i).withDayOfMonth(1);
            LocalDateTime startOfMonth = month.atStartOfDay();
            LocalDateTime endOfMonth = month.plusMonths(1).atStartOfDay().minusNanos(1);
            Double revenue = orderRepository.getRevenueBetweenDates(startOfMonth, endOfMonth);

            Map<String, Object> map = new HashMap<>();
            map.put("month", month.format(formatter));
            map.put("revenue", revenue != null ? revenue : 0.0);
            result.add(map);
        }
        return result;
    }

    @Override
    public List<Map<String, Object>> getRevenueByYear(int year) {
        List<Map<String, Object>> result = new ArrayList<>();
        for (int i = 1; i <= 12; i++) {
            LocalDate month = LocalDate.of(year, i, 1);
            LocalDateTime startOfMonth = month.atStartOfDay();
            LocalDateTime endOfMonth = month.plusMonths(1).atStartOfDay().minusNanos(1);
            Double revenue = orderRepository.getRevenueBetweenDates(startOfMonth, endOfMonth);

            Map<String, Object> map = new HashMap<>();
            map.put("month", "Tháng " + i);
            map.put("revenue", revenue != null ? revenue : 0.0);
            result.add(map);
        }
        return result;
    }

    @Override
    public Map<String, Long> getOrderCountByStatus() {
        Map<String, Long> result = new LinkedHashMap<>();
        String[] statuses = {"PENDING", "CONFIRMED", "SHIPPING", "DELIVERED", "CANCELLED"};
        for (String status : statuses) {
            result.put(status, orderRepository.countByStatus(status));
        }
        return result;
    }

    @Override
    public Map<String, Long> getProductCountByCategory() {
        Map<String, Long> result = new LinkedHashMap<>();
        List<Category> categories = categoryRepository.findAll();
        for (Category category : categories) {
            long count = productRepository.countByCategoryId(category.getId());
            if (count > 0) {
                result.put(category.getName(), count);
            }
        }
        return result;
    }

    @Override
    public List<Map<String, Object>> getTopSellingProducts(int limit) {
        List<Map<String, Object>> result = new ArrayList<>();
        try {
            List<Object[]> topProducts = orderDetailRepository.findTopSellingProducts();
            int count = 0;
            for (Object[] row : topProducts) {
                if (count >= limit) break;
                Map<String, Object> map = new HashMap<>();
                map.put("name", row[0]);
                map.put("quantity", ((Number) row[1]).longValue());
                result.add(map);
                count++;
            }
        } catch (Exception e) {
            // Return empty list if query fails
        }
        return result;
    }

    @Override
    public List<Map<String, Object>> getRecentOrders() {
        List<Order> recentOrders = orderRepository.findTop10ByOrderByCreatedAtDesc();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
        return recentOrders.stream().map(order -> {
            Map<String, Object> map = new HashMap<>();
            map.put("id", order.getId());
            map.put("customerName", order.getUser() != null ? order.getUser().getUsername() : "Khách");
            map.put("totalAmount", order.getTotalAmount() != null ? order.getTotalAmount() : 0.0);
            map.put("finalAmount", order.getFinalAmount() != null ? order.getFinalAmount() : 0.0);
            map.put("status", order.getStatus());
            map.put("createdAt", order.getCreatedAt() != null ? order.getCreatedAt().format(formatter) : "");
            map.put("payments", order.getPayments() != null ? order.getPayments() : "N/A");
            return map;
        }).collect(Collectors.toList());
    }
}
