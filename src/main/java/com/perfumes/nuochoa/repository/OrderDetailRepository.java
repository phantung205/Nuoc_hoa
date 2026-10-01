package com.perfumes.nuochoa.repository;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import com.perfumes.nuochoa.entity.OrderDetail;

public interface OrderDetailRepository extends JpaRepository<OrderDetail, Long> {
    List<OrderDetail> findByOrderId(Long orderId);

    @org.springframework.data.jpa.repository.Query("SELECT od.productVariant.product.name, SUM(od.quantity) as totalQty FROM OrderDetail od GROUP BY od.productVariant.product.id, od.productVariant.product.name ORDER BY totalQty DESC")
    List<Object[]> findTopSellingProducts();
}