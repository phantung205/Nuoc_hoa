package com.perfumes.nuochoa.controller.web;

import com.perfumes.nuochoa.dto.ProductResponseDTO;
import com.perfumes.nuochoa.entity.Brand;
import com.perfumes.nuochoa.entity.Category;
import com.perfumes.nuochoa.security.CustomUserDetails;
import com.perfumes.nuochoa.service.BrandService;
import com.perfumes.nuochoa.service.CategoryService;
import com.perfumes.nuochoa.service.ProductService;
import com.perfumes.nuochoa.service.ProductViewService;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
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
    private final ProductViewService productViewService;

    public WebProductController(ProductService productService, CategoryService categoryService,
                                BrandService brandService, ProductViewService productViewService) {
        this.productService = productService;
        this.categoryService = categoryService;
        this.brandService = brandService;
        this.productViewService = productViewService;
    }


    @GetMapping
    public String listProducts(
            @RequestParam(required = false) Long categoryId,
            @RequestParam(required = false) Long brandId,
            @RequestParam(required = false) String keyword,
            Model model) {

        List<ProductResponseDTO> products = productService.getAllActiveProducts();

        // Chỉ set pageTitle và pageDescription, KHÔNG LỌC products ở backend
        // để Javascript ở frontend có thể thực hiện lọc (khi bấm Tất cả sẽ hiện lại hết)
        if (categoryId != null) {
            Category category = categoryService.getCategoryById(categoryId);
            if (category != null) {
                model.addAttribute("pageTitle", "Danh Mục: " + category.getName());
                model.addAttribute("pageDescription", "Tuyển chọn các dòng " + category.getName().toLowerCase() + " đặc biệt.");
            }
        }

        if (brandId != null) {
            Brand brand = brandService.getBrandById(brandId);
            if (brand != null) {
                model.addAttribute("pageTitle", "Thương Hiệu: " + brand.getName());
                model.addAttribute("pageDescription", "Khám phá các sản phẩm đẳng cấp từ " + brand.getName());
            }
        }

        if (keyword != null && !keyword.trim().isEmpty()) {
            model.addAttribute("pageTitle", "Kết quả tìm kiếm: \"" + keyword + "\"");
            model.addAttribute("pageDescription", "Tìm thấy các sản phẩm phù hợp với từ khóa của bạn.");
        }

        model.addAttribute("products", products);
        model.addAttribute("categories", categoryService.getActiveCategories());
        model.addAttribute("brands", brandService.getActiveBrands());
        model.addAttribute("currentCategoryId", categoryId);
        model.addAttribute("currentBrandId", brandId);
        model.addAttribute("keyword", keyword);
        return "web/pages/products/list";
    }

    @GetMapping("/{id}")
    public String productDetail(@PathVariable Long id, Model model) {
        ProductResponseDTO product = productService.getProductById(id);

        // Ghi nhận lượt xem: Nếu user đã đăng nhập → cộng 1 điểm xem (mỗi user chỉ tính 1 lần)
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.isAuthenticated() && auth.getPrincipal() instanceof CustomUserDetails) {
            CustomUserDetails userDetails = (CustomUserDetails) auth.getPrincipal();
            productViewService.recordView(userDetails.getUser().getId(), id);
        }
        
        List<ProductResponseDTO> allProducts = productService.getAllActiveProducts();
        
        // Logic lấy sản phẩm gợi ý theo thứ tự ưu tiên: Cùng danh mục -> Cùng thương hiệu -> Giá bán gần nhất
        java.util.Comparator<ProductResponseDTO> suggestionComparator = (p1, p2) -> {
            // 1. Ưu tiên cùng thể loại (danh mục)
            boolean p1SameCat = p1.getCategoryName() != null && p1.getCategoryName().equals(product.getCategoryName());
            boolean p2SameCat = p2.getCategoryName() != null && p2.getCategoryName().equals(product.getCategoryName());
            if (p1SameCat != p2SameCat) {
                return p1SameCat ? -1 : 1;
            }

            // 2. Ưu tiên cùng thương hiệu
            boolean p1SameBrand = p1.getBrandName() != null && p1.getBrandName().equals(product.getBrandName());
            boolean p2SameBrand = p2.getBrandName() != null && p2.getBrandName().equals(product.getBrandName());
            if (p1SameBrand != p2SameBrand) {
                return p1SameBrand ? -1 : 1;
            }

            // 3. Gần giá bán nhất
            Double currentPrice = product.getMinPrice() != null ? product.getMinPrice() : 0.0;
            Double p1Price = p1.getMinPrice() != null ? p1.getMinPrice() : 0.0;
            Double p2Price = p2.getMinPrice() != null ? p2.getMinPrice() : 0.0;
            
            return Double.compare(Math.abs(p1Price - currentPrice), Math.abs(p2Price - currentPrice));
        };

        List<ProductResponseDTO> suggestedProducts = allProducts.stream()
                .filter(p -> !p.getId().equals(id))
                .sorted(suggestionComparator)
                .limit(4)
                .collect(Collectors.toList());

        model.addAttribute("product", product);
        model.addAttribute("suggestedProducts", suggestedProducts);
        return "web/pages/products/detail";
    }
}
