package com.perfumes.nuochoa.service.impl;

import com.perfumes.nuochoa.dto.ReviewDTO;
import com.perfumes.nuochoa.entity.*;
import com.perfumes.nuochoa.repository.*;
import com.perfumes.nuochoa.service.ReviewService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class ReviewServiceImpl implements ReviewService {

    private final ProductReviewRepository reviewRepository;
    private final UserRepository userRepository;
    private final ProductRepository productRepository;
    private final UserProfileRepository userProfileRepository;

    public ReviewServiceImpl(ProductReviewRepository reviewRepository,
                             UserRepository userRepository,
                             ProductRepository productRepository,
                             UserProfileRepository userProfileRepository) {
        this.reviewRepository = reviewRepository;
        this.userRepository = userRepository;
        this.productRepository = productRepository;
        this.userProfileRepository = userProfileRepository;
    }

    @Override
    @Transactional
    public ReviewDTO addReview(Long userId, Long productId, Integer rating, String comment) {
        if (reviewRepository.existsByUserIdAndProductId(userId, productId)) {
            throw new RuntimeException("Bạn đã đánh giá sản phẩm này rồi");
        }
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User không tồn tại"));
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new RuntimeException("Sản phẩm không tồn tại"));

        ProductReview review = new ProductReview();
        review.setUser(user);
        review.setProduct(product);
        review.setRating(rating);
        review.setComment(comment);
        review.setIsApproved(true);
        reviewRepository.save(review);
        return mapToDTO(review);
    }

    @Override
    public List<ReviewDTO> getApprovedReviewsByProduct(Long productId) {
        return reviewRepository.findByProductIdAndIsApprovedTrueOrderByCreatedAtDesc(productId)
                .stream().map(this::mapToDTO).collect(Collectors.toList());
    }

    @Override
    public List<ReviewDTO> getAllReviews() {
        return reviewRepository.findAllByOrderByCreatedAtDesc()
                .stream().map(this::mapToDTO).collect(Collectors.toList());
    }

    @Override
    public List<ReviewDTO> getAllReviewsByProduct(Long productId) {
        return reviewRepository.findByProductIdOrderByCreatedAtDesc(productId)
                .stream().map(this::mapToDTO).collect(Collectors.toList());
    }

    @Override
    public Double getAverageRating(Long productId) {
        Double avg = reviewRepository.getAverageRatingByProductId(productId);
        return avg != null ? Math.round(avg * 10.0) / 10.0 : 0.0;
    }

    @Override
    public long getReviewCount(Long productId) {
        return reviewRepository.countByProductId(productId);
    }

    @Override
    public boolean hasUserReviewed(Long userId, Long productId) {
        return reviewRepository.existsByUserIdAndProductId(userId, productId);
    }

    @Override
    @Transactional
    public void toggleApproval(Long reviewId) {
        ProductReview review = reviewRepository.findById(reviewId)
                .orElseThrow(() -> new RuntimeException("Đánh giá không tồn tại"));
        review.setIsApproved(!review.getIsApproved());
        reviewRepository.save(review);
    }

    @Override
    @Transactional
    public void deleteReview(Long reviewId) {
        reviewRepository.deleteById(reviewId);
    }

    private ReviewDTO mapToDTO(ProductReview review) {
        ReviewDTO dto = new ReviewDTO();
        dto.setId(review.getId());
        dto.setUserId(review.getUser().getId());
        dto.setUsername(review.getUser().getUsername());
        dto.setProductId(review.getProduct().getId());
        dto.setProductName(review.getProduct().getName());
        dto.setRating(review.getRating());
        dto.setComment(review.getComment());
        dto.setIsApproved(review.getIsApproved());
        dto.setCreatedAt(review.getCreatedAt());

        // Get avatar from UserProfile
        UserProfile profile = userProfileRepository.findById(review.getUser().getId()).orElse(null);
        if (profile != null && profile.getAvatarUrl() != null) {
            dto.setUserAvatar(profile.getAvatarUrl());
        }
        return dto;
    }
}
