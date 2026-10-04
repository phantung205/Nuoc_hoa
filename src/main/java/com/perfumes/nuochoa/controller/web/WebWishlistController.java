package com.perfumes.nuochoa.controller.web;

import com.perfumes.nuochoa.dto.ProductResponseDTO;
import com.perfumes.nuochoa.security.CustomUserDetails;
import com.perfumes.nuochoa.service.WishlistService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Controller
public class WebWishlistController {

    private final WishlistService wishlistService;

    public WebWishlistController(WishlistService wishlistService) {
        this.wishlistService = wishlistService;
    }

    /** AJAX: Toggle wishlist (add/remove) */
    @PostMapping("/api/wishlist/toggle")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> toggleWishlist(
            @AuthenticationPrincipal CustomUserDetails currentUser,
            @RequestParam Long productId) {
        Map<String, Object> response = new HashMap<>();
        if (currentUser == null) {
            response.put("success", false);
            response.put("message", "Vui lòng đăng nhập để sử dụng tính năng yêu thích");
            return ResponseEntity.status(401).body(response);
        }
        Long userId = currentUser.getUser().getId();
        boolean added = wishlistService.toggleWishlist(userId, productId);
        long count = wishlistService.countByProduct(productId);
        response.put("success", true);
        response.put("wishlisted", added);
        response.put("count", count);
        response.put("message", added ? "Đã thêm vào danh sách yêu thích" : "Đã xóa khỏi danh sách yêu thích");
        return ResponseEntity.ok(response);
    }

    /** Page: Show user's wishlist */
    @GetMapping("/profile/wishlist")
    public String showWishlist(@AuthenticationPrincipal CustomUserDetails currentUser, Model model) {
        Long userId = currentUser.getUser().getId();
        List<ProductResponseDTO> wishlistProducts = wishlistService.getWishlistProducts(userId);
        model.addAttribute("wishlistProducts", wishlistProducts);
        model.addAttribute("user", currentUser.getUser());
        return "web/pages/personal_page/wishlist";
    }
}
