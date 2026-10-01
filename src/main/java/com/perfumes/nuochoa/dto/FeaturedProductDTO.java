package com.perfumes.nuochoa.dto;

/**
 * DTO hiá»ƒn thá»‹ sáº£n pháº©m ná»•i báº­t trÃªn trang admin.
 * Chá»©a thÃ´ng tin sáº£n pháº©m kÃ¨m lÆ°á»£t xem vÃ  tráº¡ng thÃ¡i ná»•i báº­t.
 */
public class FeaturedProductDTO {

    private Long id;
    private String name;
    private String mainImageUrl;
    private String categoryName;
    private String brandName;
    private Double minPrice;
    private Double discount;
    private long viewCount;       // Tá»•ng lÆ°á»£t xem (má»—i user tÃ­nh 1 láº§n)
    private boolean featured;
    private boolean autoFeatured; // ÄÃ£ Ä‘Æ°á»£c admin Ä‘áº©y lÃªn ná»•i báº­t chÆ°a

    // ===================== GETTERS & SETTERS =====================

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getMainImageUrl() { return mainImageUrl; }
    public void setMainImageUrl(String mainImageUrl) { this.mainImageUrl = mainImageUrl; }

    public String getCategoryName() { return categoryName; }
    public void setCategoryName(String categoryName) { this.categoryName = categoryName; }

    public String getBrandName() { return brandName; }
    public void setBrandName(String brandName) { this.brandName = brandName; }

    public Double getMinPrice() { return minPrice; }
    public void setMinPrice(Double minPrice) { this.minPrice = minPrice; }

    public Double getDiscount() { return discount; }
    public void setDiscount(Double discount) { this.discount = discount; }

    public long getViewCount() { return viewCount; }
    public void setViewCount(long viewCount) { this.viewCount = viewCount; }

    public boolean isFeatured() { return featured; }
    public void setFeatured(boolean featured) { this.featured = featured; }

    public boolean isAutoFeatured() { return autoFeatured; }
    public void setAutoFeatured(boolean autoFeatured) { this.autoFeatured = autoFeatured; }
}
