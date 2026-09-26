package com.perfumes.nuochoa.service.impl;

import com.perfumes.nuochoa.dto.ProductRequestDTO;
import com.perfumes.nuochoa.dto.ProductResponseDTO;
import com.perfumes.nuochoa.dto.ProductVariantDTO;
import com.perfumes.nuochoa.entity.*;
import com.perfumes.nuochoa.repository.*;
import com.perfumes.nuochoa.service.ProductService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;

import java.nio.file.*;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class ProductServiceImpl implements ProductService {

    private final ProductRepository productRepository;
    private final ProductVariantRepository variantRepository;
    private final ProductImageRepository imageRepository;
    private final BrandRepository brandRepository;
    private final CategoryRepository categoryRepository;

    public ProductServiceImpl(ProductRepository productRepository,
                              ProductVariantRepository variantRepository,
                              ProductImageRepository imageRepository,
                              BrandRepository brandRepository,
                              CategoryRepository categoryRepository) {
        this.productRepository = productRepository;
        this.variantRepository = variantRepository;
        this.imageRepository = imageRepository;
        this.brandRepository = brandRepository;
        this.categoryRepository = categoryRepository;
    }

    @Override
    public List<ProductResponseDTO> getAllProducts() {
        return productRepository.findAll()
                .stream()
                .map(this::mapToResponseDTO)
                .collect(Collectors.toList());
    }

    @Override
    public List<ProductResponseDTO> getAllActiveProducts() {
        return productRepository.findByIsActiveTrue()
                .stream()
                .map(this::mapToResponseDTO)
                .collect(Collectors.toList());
    }

    @Override
    public ProductResponseDTO getProductById(Long id) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("KhÃ´ng tÃ¬m tháº¥y sáº£n pháº©m cÃ³ ID: " + id));
        return mapToResponseDTO(product);
    }

    @Override
    @Transactional
    public ProductResponseDTO createProduct(ProductRequestDTO requestDTO) {
        // 1. Táº¡o Ä‘á»‘i tÆ°á»£ng Product tá»« DTO
        Product product = new Product();
        product.setName(requestDTO.getName());
        product.setDescription(requestDTO.getDescription());
        product.setDiscount(requestDTO.getDiscount() != null ? requestDTO.getDiscount() : 0.0);
        product.setIsActive(requestDTO.getIsActive() != null ? requestDTO.getIsActive() : true);

        if (requestDTO.getBrandId() != null) {
            Brand brand = brandRepository.findById(requestDTO.getBrandId())
                    .orElseThrow(() -> new RuntimeException("Brand khÃ´ng tá»“n táº¡i"));
            product.setBrand(brand);
        }

        if (requestDTO.getCategoryId() != null) {
            Category category = categoryRepository.findById(requestDTO.getCategoryId())
                    .orElseThrow(() -> new RuntimeException("Category khÃ´ng tá»“n táº¡i"));
            product.setCategory(category);
        }

        Product savedProduct = productRepository.save(product);

        // 2. LÆ°u cÃ¡c biáº¿n thá»ƒ (ProductVariants)
        if (requestDTO.getVariants() != null && !requestDTO.getVariants().isEmpty()) {
            for (ProductVariantDTO vDto : requestDTO.getVariants()) {
                if (vDto.getVolume() == null && vDto.getPrice() == null) continue; // Bá» qua dÃ²ng rá»—ng

                ProductVariant variant = new ProductVariant();
                variant.setProduct(savedProduct);
                variant.setSku(vDto.getSku());
                variant.setVolume(vDto.getVolume());
                variant.setConcentration(vDto.getConcentration());
                variant.setPrice(vDto.getPrice());
                variant.setStock(vDto.getStock());

                variantRepository.save(variant);
            }
        }

        // 3. Xá»­ lÃ½ lÆ°u cÃ¡c file áº£nh chung cho toÃ n bá»™ sáº£n pháº©m
        if (requestDTO.getImageFiles() != null && !requestDTO.getImageFiles().isEmpty()) {
            List<MultipartFile> files = requestDTO.getImageFiles();
            for (int i = 0; i < files.size(); i++) {
                MultipartFile file = files.get(i);
                if (!file.isEmpty()) {
                    String fileName = saveProductImageFile(file);

                    ProductImage image = new ProductImage();
                    image.setImageUrl("/uploads/products/" + fileName);
                    image.setProduct(savedProduct);
                    image.setIsPrimary(i == requestDTO.getPrimaryImageIndex());

                    imageRepository.save(image);
                }
            }
        }

        return mapToResponseDTO(savedProduct);
    }

    @Override
    public ProductRequestDTO getProductRequestDTOById(Long id) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("KhÃ´ng tÃ¬m tháº¥y sáº£n pháº©m cÃ³ ID: " + id));

        ProductRequestDTO dto = new ProductRequestDTO();
        dto.setId(product.getId());
        dto.setName(product.getName());
        dto.setDescription(product.getDescription());
        dto.setDiscount(product.getDiscount());
        dto.setIsActive(product.getIsActive());

        if (product.getBrand() != null) {
            dto.setBrandId(product.getBrand().getId());
        }
        if (product.getCategory() != null) {
            dto.setCategoryId(product.getCategory().getId());
        }

        List<ProductVariant> variants = variantRepository.findByProductId(product.getId());
        List<ProductVariantDTO> variantDTOs = variants.stream()
            .filter(v -> v.getVolume() != null || v.getPrice() != null)
            .map(v -> {
                ProductVariantDTO vDto = new ProductVariantDTO();
                vDto.setId(v.getId());
                vDto.setSku(v.getSku());
                vDto.setVolume(v.getVolume());
                vDto.setConcentration(v.getConcentration());
                vDto.setPrice(v.getPrice());
                vDto.setStock(v.getStock());
                return vDto;
            }).collect(Collectors.toList());
        dto.setVariants(variantDTOs);

        List<ProductImage> images = imageRepository.findByProductId(product.getId());
        List<String> imageUrls = new ArrayList<>();
        int primaryIndex = 0;
        for (int i = 0; i < images.size(); i++) {
            ProductImage img = images.get(i);
            imageUrls.add(img.getImageUrl());
            if (Boolean.TRUE.equals(img.getIsPrimary())) {
                primaryIndex = i;
            }
        }
        dto.setExistingImageUrls(imageUrls);
        dto.setPrimaryImageIndex(primaryIndex);

        return dto;
    }

    @Override
    @Transactional
    public ProductResponseDTO updateProduct(Long id, ProductRequestDTO requestDTO) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("KhÃ´ng tÃ¬m tháº¥y sáº£n pháº©m cÃ³ ID: " + id));

        product.setName(requestDTO.getName());
        product.setDescription(requestDTO.getDescription());
        product.setDiscount(requestDTO.getDiscount() != null ? requestDTO.getDiscount() : 0.0);
        product.setIsActive(requestDTO.getIsActive() != null ? requestDTO.getIsActive() : true);

        if (requestDTO.getBrandId() != null) {
            Brand brand = brandRepository.findById(requestDTO.getBrandId())
                    .orElseThrow(() -> new RuntimeException("Brand khÃ´ng tá»“n táº¡i"));
            product.setBrand(brand);
        }

        if (requestDTO.getCategoryId() != null) {
            Category category = categoryRepository.findById(requestDTO.getCategoryId())
                    .orElseThrow(() -> new RuntimeException("Category khÃ´ng tá»“n táº¡i"));
            product.setCategory(category);
        }

        Product savedProduct = productRepository.save(product);

        // XÃ³a cÅ© vÃ  táº¡o má»›i biáº¿n thá»ƒ
                List<ProductVariant> existingVariants = variantRepository.findByProductId(id);
        java.util.Map<Long, ProductVariant> existingVariantMap = new java.util.HashMap<>();
        for (ProductVariant v : existingVariants) {
            existingVariantMap.put(v.getId(), v);
        }

        if (requestDTO.getVariants() != null && !requestDTO.getVariants().isEmpty()) {
            for (ProductVariantDTO vDto : requestDTO.getVariants()) {
                if (vDto.getVolume() == null && vDto.getPrice() == null) continue;

                ProductVariant variant;
                if (vDto.getId() != null && existingVariantMap.containsKey(vDto.getId())) {
                    variant = existingVariantMap.get(vDto.getId());
                    existingVariantMap.remove(vDto.getId());
                } else {
                    variant = new ProductVariant();
                    variant.setProduct(savedProduct);
                }
                
                variant.setSku(vDto.getSku());
                variant.setVolume(vDto.getVolume());
                variant.setConcentration(vDto.getConcentration());
                variant.setPrice(vDto.getPrice());
                variant.setStock(vDto.getStock() != null ? vDto.getStock() : 0);

                variantRepository.save(variant);
            }
        }
        
                for (ProductVariant variantToRemove : existingVariantMap.values()) {
            variantToRemove.setStock(0);
            variantRepository.save(variantToRemove);
        }

        // Cáº­p nháº­t láº¡i danh sÃ¡ch áº£nh:
        // BÆ°á»›c 1: Láº¥y cÃ¡c URL áº£nh hiá»‡n táº¡i cáº§n giá»¯ láº¡i
        List<String> keptUrls = requestDTO.getExistingImageUrls() != null ? requestDTO.getExistingImageUrls() : new ArrayList<>();

        // BÆ°á»›c 2: XÃ³a toÃ n bá»™ áº£nh cÅ© trong DB vÃ  xÃ³a váº­t lÃ½ nhá»¯ng áº£nh bá»‹ ngÆ°á»i dÃ¹ng gá»¡
        List<ProductImage> oldImages = imageRepository.findByProductId(id);
        for (ProductImage oldImg : oldImages) {
            if (!keptUrls.contains(oldImg.getImageUrl())) {
                deleteFile(oldImg.getImageUrl());
            }
        }
        imageRepository.deleteAll(oldImages);

        int currentImageIndex = 0;
        int primaryIndex = requestDTO.getPrimaryImageIndex() != null ? requestDTO.getPrimaryImageIndex() : 0;

        // BÆ°á»›c 3: LÆ°u láº¡i cÃ¡c áº£nh cÅ© mÃ  ngÆ°á»i dÃ¹ng khÃ´ng xÃ³a
        for (String url : keptUrls) {
            if (url != null && !url.trim().isEmpty()) {
                ProductImage img = new ProductImage();
                img.setProduct(savedProduct);
                img.setImageUrl(url);
                img.setIsPrimary(currentImageIndex == primaryIndex);
                imageRepository.save(img);
                currentImageIndex++;
            }
        }

        // BÆ°á»›c 4: ThÃªm cÃ¡c áº£nh upload má»›i
        if (requestDTO.getImageFiles() != null) {
            for (MultipartFile file : requestDTO.getImageFiles()) {
                if (!file.isEmpty()) {
                    String fileName = saveProductImageFile(file);
                    ProductImage img = new ProductImage();
                    img.setProduct(savedProduct);
                    img.setImageUrl("/uploads/products/" + fileName);
                    img.setIsPrimary(currentImageIndex == primaryIndex);
                    imageRepository.save(img);
                    currentImageIndex++;
                }
            }
        }

        return mapToResponseDTO(savedProduct);
    }

    @Override
    @Transactional
    public void deleteProduct(Long id) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy sản phẩm để xóa"));
                
        List<ProductImage> images = imageRepository.findByProductId(id);
        List<ProductVariant> variants = variantRepository.findByProductId(id);
        
        try {
            imageRepository.deleteAll(images);
            variantRepository.deleteAll(variants);
            productRepository.delete(product);
            
            for (ProductImage img : images) {
                deleteFile(img.getImageUrl());
            }
        } catch (Exception e) {
            throw new RuntimeException("Không thể xóa sản phẩm vì nó đang nằm trong đơn hàng của khách.");
        }
    }

    // ===================== HÃ€M Bá»” TRá»¢ (HELPER METHODS) =====================

    private void deleteFile(String fileUrl) {
        try {
            if (fileUrl != null && fileUrl.startsWith("/")) {
                Path path = Paths.get(fileUrl.substring(1));
                Files.deleteIfExists(path);
            }
        } catch (IOException e) {
            System.err.println("KhÃ´ng thá»ƒ xÃ³a file: " + fileUrl);
        }
    }

    /** Xá»­ lÃ½ lÆ°u file áº£nh xuá»‘ng thÆ° má»¥c váº­t lÃ½uploads/products/ */
    private String saveProductImageFile(MultipartFile file) {
        try {
            String uploadDir = "uploads/products/";
            File dir = new File(uploadDir);
            if (!dir.exists()) {
                dir.mkdirs();
            }

            String uniqueFileName = UUID.randomUUID() + "_" + file.getOriginalFilename();
            Path targetPath = Paths.get(uploadDir + uniqueFileName);

            Files.copy(file.getInputStream(), targetPath, StandardCopyOption.REPLACE_EXISTING);
            return uniqueFileName;

        } catch (IOException e) {
            throw new RuntimeException("KhÃ´ng thá»ƒ lÆ°u file áº£nh sáº£n pháº©m!", e);
        }
    }

    /** Map tá»« Entity Product sang ProductResponseDTO hiá»ƒn thá»‹ ngoÃ i view */
    private ProductResponseDTO mapToResponseDTO(Product product) {
        ProductResponseDTO dto = new ProductResponseDTO();
        dto.setId(product.getId());
        dto.setName(product.getName());
        dto.setDescription(product.getDescription());
        dto.setDiscount(product.getDiscount());
        dto.setIsActive(product.getIsActive());

        if (product.getBrand() != null) {
            dto.setBrandName(product.getBrand().getName());
        }
        if (product.getCategory() != null) {
            dto.setCategoryName(product.getCategory().getName());
        }

        // Láº¥y danh sÃ¡ch biáº¿n thá»ƒ
        List<ProductVariant> variants = variantRepository.findByProductId(product.getId());
        List<ProductVariantDTO> variantDTOs = variants.stream().map(v -> {
            ProductVariantDTO vDto = new ProductVariantDTO();
            vDto.setId(v.getId());
            vDto.setSku(v.getSku());
            vDto.setVolume(v.getVolume());
            vDto.setConcentration(v.getConcentration());
            vDto.setPrice(v.getPrice());
            vDto.setStock(v.getStock());
            return vDto;
        }).collect(Collectors.toList());

        dto.setVariants(variantDTOs);

        // Láº¥y giÃ¡ nhá» nháº¥t lÃ m giÃ¡ hiá»ƒn thá»‹
        dto.setMinPrice(variants.stream()
                .map(ProductVariant::getPrice)
                .filter(Objects::nonNull)
                .min(Double::compareTo)
                .orElse(0.0));

        // Láº¥y danh sÃ¡ch áº£nh
        List<ProductImage> images = imageRepository.findByProductId(product.getId());
        List<String> imageUrls = images.stream().map(ProductImage::getImageUrl).collect(Collectors.toList());
        dto.setImageUrls(imageUrls);

        // Láº¥y áº£nh chÃ­nh (hoáº·c áº£nh Ä‘áº§u tiÃªn lÃ m áº£nh Ä‘áº¡i diá»‡n)
        String mainImage = images.stream()
                .filter(img -> Boolean.TRUE.equals(img.getIsPrimary()))
                .map(ProductImage::getImageUrl)
                .findFirst()
                .orElse(imageUrls.isEmpty() ? "/web/images/default-product.jpg" : imageUrls.get(0));

        dto.setMainImageUrl(mainImage);

        return dto;
    }
}





