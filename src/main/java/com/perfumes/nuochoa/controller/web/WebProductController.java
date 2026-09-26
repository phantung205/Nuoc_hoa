package com.perfumes.nuochoa.controller.web;

import com.perfumes.nuochoa.dto.ProductResponseDTO;
import com.perfumes.nuochoa.entity.Brand;
import com.perfumes.nuochoa.entity.Category;
import com.perfumes.nuochoa.service.BrandService;
import com.perfumes.nuochoa.service.CategoryService;
import com.perfumes.nuochoa.service.ProductService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;
import java.util.stream.Collectors;

@Controller
@RequestMapping("/products")
public class WebProductController {

    private final ProductService productService;
    private final CategoryService categoryService;
    private final BrandService brandService;

    public WebProductController(ProductService productService, CategoryService categoryService, BrandService brandService) {
        this.productService = productService;
        this.categoryService = categoryService;
        this.brandService = brandService;
    }

    /**
     * Hiển thị danh sách sản phẩm với hỗ trợ lọc theo category, brand, keyword
     */
    @GetMapping
    public String listProducts(
            @RequestParam(required = false) Long categoryId,
            @RequestParam(required = false) Long brandId,
            @RequestParam(required = false) String keyword,
            Model model) {

        List<ProductResponseDTO> products = productService.getAllActiveProducts();

        // Lọc theo category nếu có
        if (categoryId != null) {
            Category category = categoryService.getCategoryById(categoryId);
            if (category != null) {
                products = products.stream()
                        .filter(p -> p.getCategoryName() != null && p.getCategoryName().equalsIgnoreCase(category.getName()))
                        .collect(Collectors.toList());
                model.addAttribute("pageTitle", "Danh Mục: " + category.getName());
                model.addAttribute("pageDescription", "Tuyển chọn các dòng " + category.getName().toLowerCase() + " đặc biệt.");
            }
        }

        // Lọc theo brand nếu có
        if (brandId != null) {
            Brand brand = brandService.getBrandById(brandId);
            if (brand != null) {
                products = products.stream()
                        .filter(p -> p.getBrandName() != null && p.getBrandName().equalsIgnoreCase(brand.getName()))
                        .collect(Collectors.toList());
                model.addAttribute("pageTitle", "Thương Hiệu: " + brand.getName());
                model.addAttribute("pageDescription", "Khám phá các sản phẩm đẳng cấp từ " + brand.getName());
            }
        }

        // Lọc theo keyword nếu có
        if (keyword != null && !keyword.trim().isEmpty()) {
            String kw = keyword.trim().toLowerCase();
            products = products.stream()
                    .filter(p -> p.getName().toLowerCase().contains(kw)
                            || (p.getBrandName() != null && p.getBrandName().toLowerCase().contains(kw))
                            || (p.getCategoryName() != null && p.getCategoryName().toLowerCase().contains(kw)))
                    .collect(Collectors.toList());
            model.addAttribute("pageTitle", "Kết quả tìm kiếm: \"" + keyword + "\"");
            model.addAttribute("pageDescription", "Tìm thấy " + products.size() + " sản phẩm phù hợp.");
        }

        model.addAttribute("products", products);
        model.addAttribute("categories", categoryService.getActiveCategories());
        model.addAttribute("brands", brandService.getActiveBrands());
        model.addAttribute("currentCategoryId", categoryId);
        model.addAttribute("currentBrandId", brandId);
        model.addAttribute("keyword", keyword);
        return "web/pages/products/list";
    }

    /**
     * Hiển thị trang chi tiết một sản phẩm
     */
    @GetMapping("/{id}")
    public String productDetail(@PathVariable Long id, Model model) {
        ProductResponseDTO product = productService.getProductById(id);
        model.addAttribute("product", product);
        return "web/pages/products/detail";
    }
}
