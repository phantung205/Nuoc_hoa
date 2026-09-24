package com.perfumes.nuochoa.dto;

import java.math.BigDecimal;

public class ProductVariantDTO {
    private Long id;
    private String sku;
    private BigDecimal volume;
    private BigDecimal concentration; // 1 = EDT, 2 = EDP, 3 = Parfum
    private BigDecimal price;
    private Integer stock;

    // Getters & Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getSku() { return sku; }
    public void setSku(String sku) { this.sku = sku; }

    public BigDecimal getVolume() { return volume; }
    public void setVolume(BigDecimal volume) { this.volume = volume; }

    public BigDecimal getConcentration() { return concentration; }
    public void setConcentration(BigDecimal concentration) { this.concentration = concentration; }

    public BigDecimal getPrice() { return price; }
    public void setPrice(BigDecimal price) { this.price = price; }

    public Integer getStock() { return stock; }
    public void setStock(Integer stock) { this.stock = stock; }
}