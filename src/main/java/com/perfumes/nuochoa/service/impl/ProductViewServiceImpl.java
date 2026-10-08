package com.perfumes.nuochoa.service.impl;

import com.perfumes.nuochoa.dto.FeaturedProductDTO;
import com.perfumes.nuochoa.dto.ProductResponseDTO;
import com.perfumes.nuochoa.entity.Product;
import com.perfumes.nuochoa.entity.ProductView;
import com.perfumes.nuochoa.entity.User;
import com.perfumes.nuochoa.repository.ProductRepository;
import com.perfumes.nuochoa.repository.ProductViewRepository;
import com.perfumes.nuochoa.repository.UserRepository;
import com.perfumes.nuochoa.service.ProductService;
import com.perfumes.nuochoa.service.ProductViewService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class ProductViewServiceImpl implements ProductViewService {

    private final ProductViewRepository productViewRepository;
    private final ProductRepository productRepository;
    private final UserRepository userRepository;
    private final ProductService productService;

    public ProductViewServiceImpl(ProductViewRepository productViewRepository,
                                  ProductRepository productRepository,
                                  UserRepository userRepository,
                                  ProductService productService) {
        this.productViewRepository = productViewRepository;
        this.productRepository = productRepository;
        this.userRepository = userRepository;
        this.productService = productService;
    }



    @Override
    @Transactional
    public void recordView(Long userId, Long productId) {
        // Mỗi user chỉ tính 1 lượt xem cho mỗi sản phẩm
        if (productViewRepository.existsByUserIdAndProductId(userId, productId)) {
            return; // Đã xem rồi → bỏ qua, không cộng thêm
        }

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy user"));
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy sản phẩm"));

        ProductView view = new ProductView();
        view.setUser(user);
        view.setProduct(product);
        view.setViewedAt(LocalDateTime.now());

        productViewRepository.save(view);
    }



    @Override
    public long getViewCount(Long productId) {
        return productViewRepository.countByProductId(productId);
    }



    @Override
    public List<FeaturedProductDTO> getAllProductsWithViews() {
        // Bước 1: Lấy tất cả sản phẩm active
        List<ProductResponseDTO> allProducts = productService.getAllActiveProducts();

        // Bước 2: Lấy map lượt xem (productId → viewCount)
        Map<Long, Long> viewCountMap = buildViewCountMap();

        // Bước 3: Lấy set các sản phẩm đang nổi bật
        Set<Long> featuredIds = productRepository.findByIsFeaturedTrueAndIsActiveTrue()
                .stream()
                .map(Product::getId)
                .collect(Collectors.toSet());

        // Bước 4: Ghép dữ liệu thành FeaturedProductDTO
        List<FeaturedProductDTO> result = allProducts.stream().map(p -> {
            FeaturedProductDTO dto = new FeaturedProductDTO();
            dto.setId(p.getId());
            dto.setName(p.getName());
            dto.setMainImageUrl(p.getMainImageUrl());
            dto.setCategoryName(p.getCategoryName());
            dto.setBrandName(p.getBrandName());
            dto.setMinPrice(p.getMinPrice());
            dto.setDiscount(p.getDiscount());
            dto.setViewCount(viewCountMap.getOrDefault(p.getId(), 0L));
            dto.setFeatured(featuredIds.contains(p.getId()));
            return dto;
        }).collect(Collectors.toList());

        // Sắp xếp: Nổi bật trước → Nhiều lượt xem trước
        result.sort((a, b) -> {
            if (a.isFeatured() != b.isFeatured()) {
                return a.isFeatured() ? -1 : 1;
            }
            return Long.compare(b.getViewCount(), a.getViewCount());
        });

        // Đánh dấu các sản phẩm tự động nổi bật (do hệ thống chọn để đủ 8)
        int currentFeatured = featuredIds.size();
        if (currentFeatured < 8) {
            int needed = 8 - currentFeatured;
            int marked = 0;
            for (FeaturedProductDTO dto : result) {
                if (!dto.isFeatured()) {
                    dto.setAutoFeatured(true);
                    marked++;
                    if (marked >= needed) break;
                }
            }
        }

        return result;
    }


    @Override
    @Transactional
    public void setFeatured(Long productId, boolean featured) {
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy sản phẩm"));
        product.setIsFeatured(featured);
        productRepository.save(product);
    }


    @Override
    public List<ProductResponseDTO> getTop8FeaturedProducts() {
        // Bước 1: Ưu tiên lấy sản phẩm admin đẩy lên
        List<ProductResponseDTO> featuredByAdmin = productRepository.findByIsFeaturedTrueAndIsActiveTrue()
                .stream()
                .map(p -> productService.getProductById(p.getId()))
                .collect(Collectors.toList());

        // Nếu đã đủ 8 → trả về luôn
        if (featuredByAdmin.size() >= 8) {
            return featuredByAdmin.subList(0, 8);
        }

        // Bước 2: Lấy thêm từ sản phẩm nhiều lượt xem nhất
        Set<Long> alreadyIncludedIds = featuredByAdmin.stream()
                .map(ProductResponseDTO::getId)
                .collect(Collectors.toSet());

        List<Object[]> topViewed = productViewRepository.findTopViewedProductIds();
        List<ProductResponseDTO> result = new ArrayList<>(featuredByAdmin);

        for (Object[] row : topViewed) {
            if (result.size() >= 8) break;

            Long productId = (Long) row[0];
            if (!alreadyIncludedIds.contains(productId)) {
                try {
                    result.add(productService.getProductById(productId));
                    alreadyIncludedIds.add(productId);
                } catch (Exception ignored) {
                    // Bỏ qua sản phẩm lỗi
                }
            }
        }

        // Bước 3: Nếu vẫn chưa đủ 8, bổ sung thêm sản phẩm active bất kỳ
        if (result.size() < 8) {
            List<ProductResponseDTO> allActive = productService.getAllActiveProducts();
            for (ProductResponseDTO p : allActive) {
                if (result.size() >= 8) break;
                if (!alreadyIncludedIds.contains(p.getId())) {
                    result.add(p);
                    alreadyIncludedIds.add(p.getId());
                }
            }
        }

        return result;
    }

    private Map<Long, Long> buildViewCountMap() {
        List<Object[]> rows = productViewRepository.findProductViewCounts();
        Map<Long, Long> map = new HashMap<>();
        for (Object[] row : rows) {
            map.put((Long) row[0], (Long) row[1]);
        }
        return map;
    }
}
