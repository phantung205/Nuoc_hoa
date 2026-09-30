package com.perfumes.nuochoa.dto;



public class ProductVariantDTO {
    private Long id;
    private String sku;
    private Double volume;
    private Double concentration;
    private Double price;
    private Integer stock;


    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getSku() { return sku; }
    public void setSku(String sku) { this.sku = sku; }

    public Double getVolume() { return volume; }
    public void setVolume(Double volume) { this.volume = volume; }

    public Double getConcentration() { return concentration; }
    public void setConcentration(Double concentration) { this.concentration = concentration; }

    public Double getPrice() { return price; }
    public void setPrice(Double price) { this.price = price; }

    public Integer getStock() { return stock; }
    public void setStock(Integer stock) { this.stock = stock; }
}
