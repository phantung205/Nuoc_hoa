package com.perfumes.nuochoa.repository;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import com.perfumes.nuochoa.entity.Order;

public interface OrderRepository extends JpaRepository<Order, Long> {
    List<Order> findByUserId(Long userId);
    List<Order> findByStatus(String status);
    List<Order> findByVoucherId(Long voucherId);
    List<Order> findByShippingAddressId(Long addressId);

    long countByStatus(String status);
    
    List<Order> findByCreatedAtBetween(java.time.LocalDateTime start, java.time.LocalDateTime end);
    
    long countByCreatedAtBetween(java.time.LocalDateTime start, java.time.LocalDateTime end);
    
    List<Order> findTop10ByOrderByCreatedAtDesc();

    @org.springframework.data.jpa.repository.Query("SELECT FUNCTION('DATE', o.createdAt), SUM(o.finalAmount) FROM Order o WHERE o.status != 'CANCELLED' GROUP BY FUNCTION('DATE', o.createdAt) ORDER BY FUNCTION('DATE', o.createdAt)")
    java.util.List<Object[]> sumRevenueByDate();

    @org.springframework.data.jpa.repository.Query("SELECT SUM(o.finalAmount) FROM Order o WHERE o.status != 'CANCELLED'")
    Double getTotalRevenue();

    @org.springframework.data.jpa.repository.Query("SELECT SUM(o.finalAmount) FROM Order o WHERE o.status != 'CANCELLED' AND o.createdAt BETWEEN :start AND :end")
    Double getRevenueBetweenDates(@org.springframework.data.repository.query.Param("start") java.time.LocalDateTime start, @org.springframework.data.repository.query.Param("end") java.time.LocalDateTime end);
    @org.springframework.data.jpa.repository.Query("SELECT SUM(o.finalAmount) FROM Order o WHERE o.status = :status")
    Double getRevenueByStatus(@org.springframework.data.repository.query.Param("status") String status);
}