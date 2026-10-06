package com.perfumes.nuochoa.repository;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import com.perfumes.nuochoa.entity.ProductView;

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
    @org.springframework.transaction.annotation.Transactional
    @org.springframework.data.jpa.repository.Modifying
    @org.springframework.data.jpa.repository.Query("DELETE FROM ProductView e WHERE e.user.id = :userId")
    void deleteByUserId(@org.springframework.data.repository.query.Param("userId") Long userId);
}
