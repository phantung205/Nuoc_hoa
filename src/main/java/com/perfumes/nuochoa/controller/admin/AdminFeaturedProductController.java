package com.perfumes.nuochoa.controller.admin;

import com.perfumes.nuochoa.dto.FeaturedProductDTO;
import com.perfumes.nuochoa.service.ProductViewService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;


@Controller
@RequestMapping("/admin/products/featured")
public class AdminFeaturedProductController {

    private final ProductViewService productViewService;

    public AdminFeaturedProductController(ProductViewService productViewService) {
        this.productViewService = productViewService;
    }


    @GetMapping
    public String listFeaturedProducts(Model model) {
        List<FeaturedProductDTO> products = productViewService.getAllProductsWithViews();

        // Đếm số sản phẩm đang nổi bật
        long featuredCount = products.stream().filter(FeaturedProductDTO::isFeatured).count();

        model.addAttribute("products", products);
        model.addAttribute("featuredCount", featuredCount);
        return "admin/pages/featured/list";
    }


    @PostMapping("/push/{id}")
    public String pushToFeatured(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        long featuredCount = productViewService.getAllProductsWithViews().stream()
                .filter(FeaturedProductDTO::isFeatured).count();

        if (featuredCount >= 8) {
            redirectAttributes.addFlashAttribute("errorMessage", "Đã đạt tối đa 8 sản phẩm nổi bật! Bạn cần \"Bỏ\" bớt một sản phẩm đang nổi bật để có thể thêm mới.");
            return "redirect:/admin/products/featured";
        }

        productViewService.setFeatured(id, true);
        redirectAttributes.addFlashAttribute("successMessage", "Đã đẩy sản phẩm lên nổi bật thành công!");
        return "redirect:/admin/products/featured";
    }

    @PostMapping("/remove/{id}")
    public String removeFromFeatured(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        productViewService.setFeatured(id, false);
        redirectAttributes.addFlashAttribute("successMessage", "Đã bỏ nổi bật sản phẩm thành công!");
        return "redirect:/admin/products/featured";
    }
}
