package com.perfumes.nuochoa.repository;

import com.perfumes.nuochoa.entity.ProductView;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

public interface ProductViewRepository extends JpaRepository<ProductView, Long> {

    List<ProductView> findByUserId(Long userId);

    List<ProductView> findByProductId(Long productId);

    long countByProductId(Long productId);

    boolean existsByUserIdAndProductId(Long userId, Long productId);

    @Query("SELECT pv.product.id, COUNT(pv) as viewCount " +
            "FROM ProductView pv " +
            "GROUP BY pv.product.id " +
            "ORDER BY viewCount DESC")
    List<Object[]> findProductViewCounts();

    @Query("SELECT pv.product.id, COUNT(pv) as viewCount " +
            "FROM ProductView pv " +
            "WHERE pv.product.isActive = true " +
            "GROUP BY pv.product.id " +
            "ORDER BY viewCount DESC")
    List<Object[]> findTopViewedProductIds();

    @Transactional
    @Modifying
    @Query("DELETE FROM ProductView e WHERE e.user.id = :userId")
    void deleteByUserId(@Param("userId") Long userId);
}