package com.perfumes.nuochoa.controller.web;

import com.perfumes.nuochoa.service.BrandService;
import com.perfumes.nuochoa.service.CategoryService;
import com.perfumes.nuochoa.service.ProductService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import com.perfumes.nuochoa.service.BannerService;

@Controller
public class HomeController {

    private final CategoryService categoryService;
    private final BrandService brandService;
    private final ProductService productService;
    private final BannerService bannerService;

    public HomeController(CategoryService categoryService, BrandService brandService, ProductService productService, BannerService bannerService) {
        this.categoryService = categoryService;
        this.brandService = brandService;
        this.productService = productService;
        this.bannerService = bannerService;
    }

    /** Hiển thị trang chủ với dữ liệu Category và Brand đang hoạt động. */
    @GetMapping({"/", "/home"})
    public String homePage(Model model) {
        model.addAttribute("categories", categoryService.getActiveCategories());
        model.addAttribute("brands", brandService.getActiveBrands());
        model.addAttribute("products", productService.getAllActiveProducts());
        model.addAttribute("banners", bannerService.getActiveBanners());
        return "web/index";
    }

    @GetMapping("/about")
    public String aboutPage() {
        return "web/pages/about";
    }
}