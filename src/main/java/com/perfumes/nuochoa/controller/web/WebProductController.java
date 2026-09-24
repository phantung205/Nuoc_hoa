package com.perfumes.nuochoa.controller.web;

import com.perfumes.nuochoa.dto.ProductResponseDTO;
import com.perfumes.nuochoa.service.ProductService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.List;

@Controller
@RequestMapping("/products")
public class WebProductController {

    private final ProductService productService;

    public WebProductController(ProductService productService) {
        this.productService = productService;
    }

    /**
     * Hiển thị danh sách tất cả sản phẩm cho người dùng
     */
    @GetMapping
    public String listProducts(Model model) {
        List<ProductResponseDTO> products = productService.getAllActiveProducts();
        model.addAttribute("products", products);
        return "web/pages/products/list";
    }

    /**
     * Hiển thị trang chi tiết một sản phẩm
     */
    @GetMapping("/{id}")
    public String productDetail(@PathVariable Long id, Model model) {
        ProductResponseDTO product = productService.getProductById(id);
        model.addAttribute("product", product);
        
        // Bạn có thể lấy thêm các sản phẩm liên quan ở đây nếu cần
        
        return "web/pages/products/detail";
    }
}
