package com.perfumes.nuochoa.controller.web;

import com.perfumes.nuochoa.security.CustomUserDetails;
import com.perfumes.nuochoa.service.ReviewService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/reviews")
public class ReviewApiController {

    private final ReviewService reviewService;

    public ReviewApiController(ReviewService reviewService) {
        this.reviewService = reviewService;
    }

    @PostMapping("/add")
    public ResponseEntity<Map<String, Object>> addReview(
            @AuthenticationPrincipal CustomUserDetails currentUser,
            @RequestParam Long productId,
            @RequestParam Integer rating,
            @RequestParam String comment) {

        Map<String, Object> response = new HashMap<>();

        if (currentUser == null) {
            response.put("success", false);
            response.put("message", "Vui lÃ²ng Ä‘Äƒng nháº­p Ä‘á»ƒ Ä‘Ã¡nh giÃ¡");
            return ResponseEntity.status(401).body(response);
        }

        try {
            Long userId = currentUser.getUser().getId();
            reviewService.addReview(userId, productId, rating, comment);
            response.put("success", true);
            response.put("message", "ÄÃ¡nh giÃ¡ cá»§a báº¡n Ä‘Ã£ Ä‘Æ°á»£c gá»­i thÃ nh cÃ´ng!");
            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            response.put("success", false);
            response.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }
}

