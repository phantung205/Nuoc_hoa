package com.perfumes.nuochoa.repository;

import com.perfumes.nuochoa.entity.OrderDetail;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface OrderDetailRepository extends JpaRepository<OrderDetail, Long> {

    List<OrderDetail> findByOrderId(Long orderId);

    @Query("SELECT od.productVariant.product.name, SUM(od.quantity) as totalQty " +
            "FROM OrderDetail od " +
            "GROUP BY od.productVariant.product.id, od.productVariant.product.name " +
            "ORDER BY totalQty DESC")
    List<Object[]> findTopSellingProducts();

    @Query("SELECT COUNT(od) > 0 FROM OrderDetail od " +
            "WHERE od.order.user.id = :userId " +
            "AND od.productVariant.product.id = :productId " +
            "AND od.order.status = 'DELIVERED'")
    boolean existsByUserIdAndProductId(@Param("userId") Long userId, @Param("productId") Long productId);
}