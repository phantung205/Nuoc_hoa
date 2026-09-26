package com.perfumes.nuochoa.controller.web;

import com.perfumes.nuochoa.dto.ProductResponseDTO;
import com.perfumes.nuochoa.entity.Brand;
import com.perfumes.nuochoa.repository.BrandRepository;
import com.perfumes.nuochoa.service.ProductService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.List;
import java.util.stream.Collectors;

@Controller
@RequestMapping("/brands")
public class WebBrandController {

    private final ProductService productService;
    private final BrandRepository brandRepository;

    public WebBrandController(ProductService productService, BrandRepository brandRepository) {
        this.productService = productService;
        this.brandRepository = brandRepository;
    }

    @GetMapping("/{id}")
    public String listProductsByBrand(@PathVariable Long id, Model model) {
        Brand brand = brandRepository.findById(id).orElse(null);
        
        if (brand == null || !brand.getIsActive()) {
            return "redirect:/products";
        }
        
        // Lấy tất cả sản phẩm
        List<ProductResponseDTO> allProducts = productService.getAllActiveProducts();
        
        // Lọc sản phẩm theo Thương hiệu
        List<ProductResponseDTO> brandProducts = allProducts.stream()
                .filter(p -> p.getBrandName() != null && p.getBrandName().equalsIgnoreCase(brand.getName()))
                .collect(Collectors.toList());
                
        model.addAttribute("products", brandProducts);
        model.addAttribute("pageTitle", "Thương Hiệu: " + brand.getName());
        model.addAttribute("pageDescription", "Khám phá các dòng sản phẩm đẳng cấp từ thương hiệu " + brand.getName());
        
        return "web/pages/products/list";
    }
}
