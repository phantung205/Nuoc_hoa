package com.perfumes.nuochoa.service;

import com.perfumes.nuochoa.dto.ReviewDTO;
import java.util.List;

public interface ReviewService {
    ReviewDTO addReview(Long userId, Long productId, Integer rating, String comment);
    List<ReviewDTO> getApprovedReviewsByProduct(Long productId);
    List<ReviewDTO> getAllReviews();
    List<ReviewDTO> getAllReviewsByProduct(Long productId);
    Double getAverageRating(Long productId);
    long getReviewCount(Long productId);
    boolean hasUserReviewed(Long userId, Long productId);
    void toggleApproval(Long reviewId);
    void deleteReview(Long reviewId);
    boolean hasUserPurchasedProduct(Long userId, Long productId);
}
