package com.perfumes.nuochoa.controller.staff;

import com.perfumes.nuochoa.dto.FeaturedProductDTO;
import com.perfumes.nuochoa.service.ProductViewService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;


@Controller
@RequestMapping("/staff/products/featured")
public class StaffFeaturedProductController {

    private final ProductViewService productViewService;

    public StaffFeaturedProductController(ProductViewService productViewService) {
        this.productViewService = productViewService;
    }


    @GetMapping
    public String listFeaturedProducts(Model model) {
        List<FeaturedProductDTO> products = productViewService.getAllProductsWithViews();

        // Đếm số sản phẩm đang nổi bật
        long featuredCount = products.stream().filter(FeaturedProductDTO::isFeatured).count();

        model.addAttribute("products", products);
        model.addAttribute("featuredCount", featuredCount);
        return "staff/pages/featured/list";
    }


    @PostMapping("/push/{id}")
    public String pushToFeatured(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        long featuredCount = productViewService.getAllProductsWithViews().stream()
                .filter(FeaturedProductDTO::isFeatured).count();

        if (featuredCount >= 8) {
            redirectAttributes.addFlashAttribute("errorMessage", "Đã đạt tối đa 8 sản phẩm nổi bật! Bạn cần \"Bỏ\" bớt một sản phẩm đang nổi bật để có thể thêm mới.");
            return "redirect:/staff/products/featured";
        }

        productViewService.setFeatured(id, true);
        redirectAttributes.addFlashAttribute("successMessage", "Đã đẩy sản phẩm lên nổi bật thành công!");
        return "redirect:/staff/products/featured";
    }

    @PostMapping("/remove/{id}")
    public String removeFromFeatured(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        productViewService.setFeatured(id, false);
        redirectAttributes.addFlashAttribute("successMessage", "Đã bỏ nổi bật sản phẩm thành công!");
        return "redirect:/staff/products/featured";
    }
}

