package com.perfumes.nuochoa.repository;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import com.perfumes.nuochoa.entity.ProductView;

public interface ProductViewRepository extends JpaRepository<ProductView, Long> {
    List<ProductView> findByUserId(Long userId);
    List<ProductView> findByProductId(Long productId);

    /** Đếm tổng lượt xem của 1 sản phẩm */
    long countByProductId(Long productId);

    /** Kiểm tra user đã xem sản phẩm này chưa (tránh đếm trùng) */
    boolean existsByUserIdAndProductId(Long userId, Long productId);

    /**
     * Lấy danh sách productId kèm tổng lượt xem, sắp xếp giảm dần.
     * Trả về List<Object[]> trong đó [0]=productId (Long), [1]=viewCount (Long)
     */
    @Query("SELECT pv.product.id, COUNT(pv) as viewCount " +
           "FROM ProductView pv " +
           "GROUP BY pv.product.id " +
           "ORDER BY viewCount DESC")
    List<Object[]> findProductViewCounts();

    /**
     * Lấy top N sản phẩm có nhiều lượt xem nhất (chỉ lấy sản phẩm active).
     */
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
