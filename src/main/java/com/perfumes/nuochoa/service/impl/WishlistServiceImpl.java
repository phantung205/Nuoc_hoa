package com.perfumes.nuochoa.service.impl;

import com.perfumes.nuochoa.dto.ProductResponseDTO;
import com.perfumes.nuochoa.entity.Product;
import com.perfumes.nuochoa.entity.User;
import com.perfumes.nuochoa.entity.Wishlist;
import com.perfumes.nuochoa.repository.ProductRepository;
import com.perfumes.nuochoa.repository.UserRepository;
import com.perfumes.nuochoa.repository.WishlistRepository;
import com.perfumes.nuochoa.service.ProductService;
import com.perfumes.nuochoa.service.WishlistService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class WishlistServiceImpl implements WishlistService {

    private final WishlistRepository wishlistRepository;
    private final ProductRepository productRepository;
    private final UserRepository userRepository;
    private final ProductService productService;

    public WishlistServiceImpl(WishlistRepository wishlistRepository,
                               ProductRepository productRepository,
                               UserRepository userRepository,
                               ProductService productService) {
        this.wishlistRepository = wishlistRepository;
        this.productRepository = productRepository;
        this.userRepository = userRepository;
        this.productService = productService;
    }

    @Override
    @Transactional
    public boolean toggleWishlist(Long userId, Long productId) {
        if (wishlistRepository.existsByUserIdAndProductId(userId, productId)) {
            wishlistRepository.deleteByUserIdAndProductId(userId, productId);
            return false; // removed
        } else {
            User user = userRepository.findById(userId)
                    .orElseThrow(() -> new RuntimeException("User không tồn tại"));
            Product product = productRepository.findById(productId)
                    .orElseThrow(() -> new RuntimeException("Sản phẩm không tồn tại"));
            Wishlist wishlist = new Wishlist();
            wishlist.setUser(user);
            wishlist.setProduct(product);
            wishlistRepository.save(wishlist);
            return true; // added
        }
    }

    @Override
    public boolean isWishlisted(Long userId, Long productId) {
        return wishlistRepository.existsByUserIdAndProductId(userId, productId);
    }

    @Override
    public long countByProduct(Long productId) {
        return wishlistRepository.countByProductId(productId);
    }

    @Override
    public List<ProductResponseDTO> getWishlistProducts(Long userId) {
        List<Wishlist> wishlists = wishlistRepository.findByUserIdOrderByCreatedAtDesc(userId);
        return wishlists.stream()
                .map(w -> productService.getProductById(w.getProduct().getId()))
                .collect(Collectors.toList());
    }
}
