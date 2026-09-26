package com.perfumes.nuochoa.controller.web;

import com.perfumes.nuochoa.dto.ProductResponseDTO;
import com.perfumes.nuochoa.service.BrandService;
import com.perfumes.nuochoa.service.CategoryService;
import com.perfumes.nuochoa.service.ProductService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import java.util.List;
import java.util.stream.Collectors;

@Controller
public class PromotionController {

    private final ProductService productService;
    private final CategoryService categoryService;
    private final BrandService brandService;

    public PromotionController(ProductService productService, CategoryService categoryService, BrandService brandService) {
        this.productService = productService;
        this.categoryService = categoryService;
        this.brandService = brandService;
    }

    /**
     * Trang Ưu Đãi - Hiển thị các sản phẩm đang giảm giá
     */
    @GetMapping("/promotions")
    public String promotions(Model model) {
        List<ProductResponseDTO> allProducts = productService.getAllActiveProducts();

        // Lọc sản phẩm có discount > 0
        List<ProductResponseDTO> discountedProducts = allProducts.stream()
                .filter(p -> p.getDiscount() != null && p.getDiscount() > 0)
                .collect(Collectors.toList());

        model.addAttribute("products", discountedProducts);
        model.addAttribute("allProductsCount", allProducts.size());
        model.addAttribute("categories", categoryService.getActiveCategories());
        model.addAttribute("brands", brandService.getActiveBrands());
        return "web/pages/products/promotions";
    }
}
