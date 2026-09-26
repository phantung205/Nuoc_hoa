package com.perfumes.nuochoa.dto;

public class CartItemDto {
    private Long itemId;
    private Long productId;
    private String productName;
    private String imageUrl;
    private Double volume;
    private Double concentration;
    private Double price;
    private Integer quantity;
    private Integer stock;
    private Double totalPrice;

    // Getters and Setters
    public Long getItemId() { return itemId; }
    public void setItemId(Long itemId) { this.itemId = itemId; }

    public Long getProductId() { return productId; }
    public void setProductId(Long productId) { this.productId = productId; }

    public String getProductName() { return productName; }
    public void setProductName(String productName) { this.productName = productName; }

    public String getImageUrl() { return imageUrl; }
    public void setImageUrl(String imageUrl) { this.imageUrl = imageUrl; }

    public Double getVolume() { return volume; }
    public void setVolume(Double volume) { this.volume = volume; }

    public Double getConcentration() { return concentration; }
    public void setConcentration(Double concentration) { this.concentration = concentration; }

    public Double getPrice() { return price; }
    public void setPrice(Double price) { this.price = price; }

    public Integer getQuantity() { return quantity; }
    public void setQuantity(Integer quantity) { this.quantity = quantity; }

    public Double getTotalPrice() { return totalPrice; }
    public void setTotalPrice(Double totalPrice) { this.totalPrice = totalPrice; }
    public Integer getStock() { return stock; }
    public void setStock(Integer stock) { this.stock = stock; }
}

