package com.perfumes.nuochoa.controller.web;

import com.perfumes.nuochoa.dto.ProductResponseDTO;
import com.perfumes.nuochoa.security.CustomUserDetails;
import com.perfumes.nuochoa.service.ProductService;
import com.perfumes.nuochoa.service.WishlistService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Controller
public class WebWishlistController {

    private final WishlistService wishlistService;
    private final ProductService productService;

    public WebWishlistController(WishlistService wishlistService, ProductService productService) {
        this.wishlistService = wishlistService;
        this.productService = productService;
    }

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

    @GetMapping("/profile/wishlist")
    public String showWishlist(@AuthenticationPrincipal CustomUserDetails currentUser, Model model) {
        Long userId = currentUser.getUser().getId();
        List<ProductResponseDTO> wishlistProducts = wishlistService.getWishlistProducts(userId);
        model.addAttribute("wishlistProducts", wishlistProducts);
        model.addAttribute("user", currentUser.getUser());

        List<ProductResponseDTO> allProducts = productService.getAllActiveProducts();
        
        Set<Long> wishlistProductIds = wishlistProducts.stream()
                .map(ProductResponseDTO::getId)
                .collect(Collectors.toSet());
        
        Set<String> likedCategories = wishlistProducts.stream()
                .map(ProductResponseDTO::getCategoryName)
                .filter(name -> name != null)
                .collect(Collectors.toSet());
                
        Set<String> likedBrands = wishlistProducts.stream()
                .map(ProductResponseDTO::getBrandName)
                .filter(name -> name != null)
                .collect(Collectors.toSet());

        List<ProductResponseDTO> suggestedProducts = allProducts.stream()
                .filter(p -> !wishlistProductIds.contains(p.getId()))
                .sorted((p1, p2) -> {
                    int score1 = 0;
                    if (p1.getCategoryName() != null && likedCategories.contains(p1.getCategoryName())) score1 += 2;
                    if (p1.getBrandName() != null && likedBrands.contains(p1.getBrandName())) score1 += 1;
                    
                    int score2 = 0;
                    if (p2.getCategoryName() != null && likedCategories.contains(p2.getCategoryName())) score2 += 2;
                    if (p2.getBrandName() != null && likedBrands.contains(p2.getBrandName())) score2 += 1;
                    
                    if (score1 != score2) {
                        return Integer.compare(score2, score1);
                    }
                    Double disc1 = p1.getDiscount() != null ? p1.getDiscount() : 0.0;
                    Double disc2 = p2.getDiscount() != null ? p2.getDiscount() : 0.0;
                    if (!disc1.equals(disc2)) {
                        return Double.compare(disc2, disc1);
                    }
                    return Long.compare(p2.getId(), p1.getId());
                })
                .limit(4) // Lấy 4 sản phẩm hiển thị đẹp trên 1 dòng
                .collect(Collectors.toList());

        model.addAttribute("suggestedProducts", suggestedProducts);

        return "web/pages/personal_page/wishlist";
    }
}
