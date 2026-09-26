package com.perfumes.nuochoa.controller.web;

import com.perfumes.nuochoa.dto.ProductResponseDTO;
import com.perfumes.nuochoa.entity.Category;
import com.perfumes.nuochoa.repository.CategoryRepository;
import com.perfumes.nuochoa.service.ProductService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.List;
import java.util.stream.Collectors;

@Controller
@RequestMapping("/categories")
public class WebCategoryController {

    private final ProductService productService;
    private final CategoryRepository categoryRepository;

    public WebCategoryController(ProductService productService, CategoryRepository categoryRepository) {
        this.productService = productService;
        this.categoryRepository = categoryRepository;
    }

    @GetMapping("/{id}")
    public String listProductsByCategory(@PathVariable Long id, Model model) {
        Category category = categoryRepository.findById(id).orElse(null);
        
        if (category == null || !category.getIsActive()) {
            return "redirect:/products";
        }
        
        // Lấy tất cả sản phẩm
        List<ProductResponseDTO> allProducts = productService.getAllActiveProducts();
        
        // Lọc sản phẩm theo Category
        List<ProductResponseDTO> categoryProducts = allProducts.stream()
                .filter(p -> p.getCategoryName() != null && p.getCategoryName().equalsIgnoreCase(category.getName()))
                .collect(Collectors.toList());
                
        model.addAttribute("products", categoryProducts);
        model.addAttribute("pageTitle", "Danh Mục: " + category.getName());
        model.addAttribute("pageDescription", "Tuyển chọn các dòng " + category.getName().toLowerCase() + " đặc biệt từ cửa hàng của chúng tôi.");
        
        return "web/pages/products/list";
    }
}
