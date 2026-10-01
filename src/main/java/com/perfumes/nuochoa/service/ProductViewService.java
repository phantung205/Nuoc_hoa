package com.perfumes.nuochoa.service;

import com.perfumes.nuochoa.dto.FeaturedProductDTO;
import com.perfumes.nuochoa.dto.ProductResponseDTO;

import java.util.List;

public interface ProductViewService {


    void recordView(Long userId, Long productId);

    long getViewCount(Long productId);

    List<FeaturedProductDTO> getAllProductsWithViews();

    void setFeatured(Long productId, boolean featured);

    List<ProductResponseDTO> getTop8FeaturedProducts();
}
