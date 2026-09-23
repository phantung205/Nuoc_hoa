package com.perfumes.nuochoa.controller.web;

import com.perfumes.nuochoa.service.BrandService;
import com.perfumes.nuochoa.service.CategoryService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class HomeController {

    private final CategoryService categoryService;
    private final BrandService brandService;

    public HomeController(CategoryService categoryService, BrandService brandService) {
        this.categoryService = categoryService;
        this.brandService = brandService;
    }

    /** Hiển thị trang chủ với dữ liệu Category và Brand đang hoạt động. */
    @GetMapping({"/", "/home"})
    public String homePage(Model model) {
        model.addAttribute("categories", categoryService.getActiveCategories());
        model.addAttribute("brands", brandService.getActiveBrands());

        return "web/index";
    }
}