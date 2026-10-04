package com.perfumes.nuochoa.service;

import com.perfumes.nuochoa.dto.ProductResponseDTO;
import java.util.List;

public interface WishlistService {
    boolean toggleWishlist(Long userId, Long productId);
    boolean isWishlisted(Long userId, Long productId);
    long countByProduct(Long productId);
    List<ProductResponseDTO> getWishlistProducts(Long userId);
}
