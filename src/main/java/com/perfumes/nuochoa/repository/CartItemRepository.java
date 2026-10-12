package com.perfumes.nuochoa.repository;

import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;
import com.perfumes.nuochoa.entity.CartItem;

public interface CartItemRepository extends JpaRepository<CartItem, Long> {
    List<CartItem> findByCartId(Long cartId);
    Optional<CartItem> findByCartIdAndProductVariantId(Long cartId, Long variantId);
    void deleteByCartId(Long cartId);

    @Transactional
    @Modifying
    @Query("DELETE FROM CartItem ci WHERE ci.productVariant.id IN :variantIds")
    void deleteByProductVariantIdIn(@Param("variantIds") List<Long> variantIds);
}