package com.perfumes.nuochoa.dto;

import org.springframework.web.multipart.MultipartFile;

import java.util.ArrayList;
import java.util.List;

public class ProductRequestDTO {
    private Long id;
    private String name;
    private String description;
    private Double discount;
    private Boolean isActive = true;
    private Long brandId;
    private Long categoryId;

    // Danh sách biến thể đi kèm sản phẩm
    private List<ProductVariantDTO> variants = new ArrayList<>();

    // Danh sách file ảnh tải lên từ máy tính
    private List<MultipartFile> imageFiles = new ArrayList<>();

    // Danh sách ảnh đã có từ trước (dùng cho form Edit)
    private List<String> existingImageUrls = new ArrayList<>();

    // Vị trí của ảnh chính trong danh sách (Ví dụ: 0 là ảnh đầu tiên)
    private Integer primaryImageIndex = 0;

    // Getters & Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public Double getDiscount() { return discount; }
    public void setDiscount(Double discount) { this.discount = discount; }

    public Boolean getIsActive() { return isActive; }
    public void setIsActive(Boolean isActive) { this.isActive = isActive; }

    public Long getBrandId() { return brandId; }
    public void setBrandId(Long brandId) { this.brandId = brandId; }

    public Long getCategoryId() { return categoryId; }
    public void setCategoryId(Long categoryId) { this.categoryId = categoryId; }

    public List<ProductVariantDTO> getVariants() { return variants; }
    public void setVariants(List<ProductVariantDTO> variants) { this.variants = variants; }

    public List<MultipartFile> getImageFiles() { return imageFiles; }
    public void setImageFiles(List<MultipartFile> imageFiles) { this.imageFiles = imageFiles; }

    public List<String> getExistingImageUrls() { return existingImageUrls; }
    public void setExistingImageUrls(List<String> existingImageUrls) { this.existingImageUrls = existingImageUrls; }

    public Integer getPrimaryImageIndex() { return primaryImageIndex; }
    public void setPrimaryImageIndex(Integer primaryImageIndex) { this.primaryImageIndex = primaryImageIndex; }
}
