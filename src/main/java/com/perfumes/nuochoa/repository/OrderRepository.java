package com.perfumes.nuochoa.repository;

import com.perfumes.nuochoa.entity.Order;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;

public interface OrderRepository extends JpaRepository<Order, Long> {

    List<Order> findByUserId(Long userId);

    List<Order> findByStatus(String status);

    List<Order> findByVoucherId(Long voucherId);

    List<Order> findByShippingAddressId(Long addressId);

    long countByStatus(String status);

    List<Order> findByCreatedAtBetween(LocalDateTime start, LocalDateTime end);

    long countByCreatedAtBetween(LocalDateTime start, LocalDateTime end);

    List<Order> findTop10ByOrderByCreatedAtDesc();

    @Query("SELECT FUNCTION('DATE', o.createdAt), SUM(o.finalAmount) " +
            "FROM Order o WHERE o.status != 'CANCELLED' " +
            "GROUP BY FUNCTION('DATE', o.createdAt) " +
            "ORDER BY FUNCTION('DATE', o.createdAt)")
    List<Object[]> sumRevenueByDate();

    @Query("SELECT SUM(o.finalAmount) FROM Order o WHERE o.status != 'CANCELLED'")
    Double getTotalRevenue();

    @Query("SELECT SUM(o.finalAmount) FROM Order o " +
            "WHERE o.status != 'CANCELLED' AND o.createdAt BETWEEN :start AND :end")
    Double getRevenueBetweenDates(@Param("start") LocalDateTime start, @Param("end") LocalDateTime end);

    @Query("SELECT SUM(o.finalAmount) FROM Order o WHERE o.status = :status")
    Double getRevenueByStatus(@Param("status") String status);
}