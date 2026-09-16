package com.perfumes.nuochoa.repository;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import com.perfumes.nuochoa.entity.ProductView;

public interface ProductViewRepository extends JpaRepository<ProductView, Long> {
    List<ProductView> findByUserId(Long userId);
    List<ProductView> findByProductId(Long productId);
}