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
                .orElseThrow(() -> new RuntimeException("Không tìm thấy sản phẩm có ID: " + id));
        return mapToResponseDTO(product);
    }

    @Override
    @Transactional
    public ProductResponseDTO createProduct(ProductRequestDTO requestDTO) {
        // 1. Tạo đối tượng Product từ DTO
        Product product = new Product();
        product.setName(requestDTO.getName());
        product.setDescription(requestDTO.getDescription());
        product.setDiscount(requestDTO.getDiscount() != null ? requestDTO.getDiscount() : 0.0);
        product.setIsActive(requestDTO.getIsActive() != null ? requestDTO.getIsActive() : true);

        if (requestDTO.getBrandId() != null) {
            Brand brand = brandRepository.findById(requestDTO.getBrandId())
                    .orElseThrow(() -> new RuntimeException("Brand không tồn tại"));
            product.setBrand(brand);
        }

        if (requestDTO.getCategoryId() != null) {
            Category category = categoryRepository.findById(requestDTO.getCategoryId())
                    .orElseThrow(() -> new RuntimeException("Category không tồn tại"));
            product.setCategory(category);
        }

        Product savedProduct = productRepository.save(product);

        // 2. Lưu các biến thể (ProductVariants)
        if (requestDTO.getVariants() != null && !requestDTO.getVariants().isEmpty()) {
            for (ProductVariantDTO vDto : requestDTO.getVariants()) {
                if (vDto.getVolume() == null && vDto.getPrice() == null) continue; // Bỏ qua dòng rỗng

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

        // 3. Xử lý lưu các file ảnh chung cho toàn bộ sản phẩm
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
                .orElseThrow(() -> new RuntimeException("Không tìm thấy sản phẩm có ID: " + id));

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
                .orElseThrow(() -> new RuntimeException("Không tìm thấy sản phẩm có ID: " + id));

        product.setName(requestDTO.getName());
        product.setDescription(requestDTO.getDescription());
        product.setDiscount(requestDTO.getDiscount() != null ? requestDTO.getDiscount() : 0.0);
        product.setIsActive(requestDTO.getIsActive() != null ? requestDTO.getIsActive() : true);

        if (requestDTO.getBrandId() != null) {
            Brand brand = brandRepository.findById(requestDTO.getBrandId())
                    .orElseThrow(() -> new RuntimeException("Brand không tồn tại"));
            product.setBrand(brand);
        }

        if (requestDTO.getCategoryId() != null) {
            Category category = categoryRepository.findById(requestDTO.getCategoryId())
                    .orElseThrow(() -> new RuntimeException("Category không tồn tại"));
            product.setCategory(category);
        }

        Product savedProduct = productRepository.save(product);

        // Xóa cũ và tạo mới biến thể
        List<ProductVariant> existingVariants = variantRepository.findByProductId(id);
        variantRepository.deleteAll(existingVariants);

        if (requestDTO.getVariants() != null && !requestDTO.getVariants().isEmpty()) {
            for (ProductVariantDTO vDto : requestDTO.getVariants()) {
                if (vDto.getVolume() == null && vDto.getPrice() == null) continue; // Bỏ qua dòng rỗng

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

        // Cập nhật lại danh sách ảnh:
        // Bước 1: Lấy các URL ảnh hiện tại cần giữ lại
        List<String> keptUrls = requestDTO.getExistingImageUrls() != null ? requestDTO.getExistingImageUrls() : new ArrayList<>();

        // Bước 2: Xóa toàn bộ ảnh cũ trong DB và xóa vật lý những ảnh bị người dùng gỡ
        List<ProductImage> oldImages = imageRepository.findByProductId(id);
        for (ProductImage oldImg : oldImages) {
            if (!keptUrls.contains(oldImg.getImageUrl())) {
                deleteFile(oldImg.getImageUrl());
            }
        }
        imageRepository.deleteAll(oldImages);

        int currentImageIndex = 0;
        int primaryIndex = requestDTO.getPrimaryImageIndex() != null ? requestDTO.getPrimaryImageIndex() : 0;

        // Bước 3: Lưu lại các ảnh cũ mà người dùng không xóa
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

        // Bước 4: Thêm các ảnh upload mới
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
                
        // Xóa cứng: Xóa các biến thể
        List<ProductVariant> variants = variantRepository.findByProductId(id);
        variantRepository.deleteAll(variants);

        // Xóa cứng: Xóa ảnh trong DB và xóa file vật lý
        List<ProductImage> images = imageRepository.findByProductId(id);
        for (ProductImage img : images) {
            deleteFile(img.getImageUrl());
        }
        imageRepository.deleteAll(images);

        // Xóa sản phẩm
        productRepository.delete(product);
    }

    // ===================== HÀM BỔ TRỢ (HELPER METHODS) =====================

    private void deleteFile(String fileUrl) {
        try {
            if (fileUrl != null && fileUrl.startsWith("/")) {
                Path path = Paths.get(fileUrl.substring(1));
                Files.deleteIfExists(path);
            }
        } catch (IOException e) {
            System.err.println("Không thể xóa file: " + fileUrl);
        }
    }

    /** Xử lý lưu file ảnh xuống thư mục vật lýuploads/products/ */
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
            throw new RuntimeException("Không thể lưu file ảnh sản phẩm!", e);
        }
    }

    /** Map từ Entity Product sang ProductResponseDTO hiển thị ngoài view */
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

        // Lấy danh sách biến thể
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

        // Lấy giá nhỏ nhất làm giá hiển thị
        dto.setMinPrice(variants.stream()
                .map(ProductVariant::getPrice)
                .filter(Objects::nonNull)
                .min(Double::compareTo)
                .orElse(0.0));

        // Lấy danh sách ảnh
        List<ProductImage> images = imageRepository.findByProductId(product.getId());
        List<String> imageUrls = images.stream().map(ProductImage::getImageUrl).collect(Collectors.toList());
        dto.setImageUrls(imageUrls);

        // Lấy ảnh chính (hoặc ảnh đầu tiên làm ảnh đại diện)
        String mainImage = images.stream()
                .filter(img -> Boolean.TRUE.equals(img.getIsPrimary()))
                .map(ProductImage::getImageUrl)
                .findFirst()
                .orElse(imageUrls.isEmpty() ? "/web/images/default-product.jpg" : imageUrls.get(0));

        dto.setMainImageUrl(mainImage);

        return dto;
    }
}
