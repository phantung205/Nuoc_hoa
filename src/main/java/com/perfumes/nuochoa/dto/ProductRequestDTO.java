package com.perfumes.nuochoa.dto;

import org.springframework.web.multipart.MultipartFile;
import jakarta.validation.constraints.*;
import jakarta.validation.Valid;

import java.util.ArrayList;
import java.util.List;

public class ProductRequestDTO {
    private Long id;
    @NotBlank(message = "Tên sản phẩm không được để trống")
    @Size(max = 200, message = "Tên sản phẩm không được vượt quá 200 ký tự")
    private String name;
    private String description;
    private Double discount;
    private Boolean isActive = true;
    @NotNull(message = "Vui lòng chọn thương hiệu")
    private Long brandId;
    @NotNull(message = "Vui lòng chọn danh mục")
    private Long categoryId;

    @Valid
    @NotEmpty(message = "Phải có ít nhất một biến thể (Dung tích & Nồng độ)")
    private List<ProductVariantDTO> variants = new ArrayList<>();

    // Fields for Edit Page
    private List<MultipartFile> imageFiles = new ArrayList<>();
    private List<String> existingImageUrls = new ArrayList<>();
    private Integer primaryImageIndex = 0;

    // Fields for Add Page
    private MultipartFile mainImageFile;
    private List<MultipartFile> subImageFiles = new ArrayList<>();

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

    public MultipartFile getMainImageFile() { return mainImageFile; }
    public void setMainImageFile(MultipartFile mainImageFile) { this.mainImageFile = mainImageFile; }

    public List<MultipartFile> getSubImageFiles() { return subImageFiles; }
    public void setSubImageFiles(List<MultipartFile> subImageFiles) { this.subImageFiles = subImageFiles; }
}
