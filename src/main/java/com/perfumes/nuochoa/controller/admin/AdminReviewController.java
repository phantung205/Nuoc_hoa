package com.perfumes.nuochoa.controller.admin;

import com.perfumes.nuochoa.service.ReviewService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/admin/reviews")
public class AdminReviewController {

    private final ReviewService reviewService;

    public AdminReviewController(ReviewService reviewService) {
        this.reviewService = reviewService;
    }

    @GetMapping
    public String listReviews(Model model) {
        model.addAttribute("reviews", reviewService.getAllReviews());
        model.addAttribute("activeMenu", "reviews");
        return "admin/pages/reviews/list";
    }

    @PostMapping("/{id}/toggle")
    public String toggleApproval(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        reviewService.toggleApproval(id);
        redirectAttributes.addFlashAttribute("successMessage", "Đã cập nhật trạng thái đánh giá");
        return "redirect:/admin/reviews";
    }

    @PostMapping("/{id}/delete")
    public String deleteReview(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        reviewService.deleteReview(id);
        redirectAttributes.addFlashAttribute("successMessage", "Đã xóa đánh giá thành công");
        return "redirect:/admin/reviews";
    }
}
