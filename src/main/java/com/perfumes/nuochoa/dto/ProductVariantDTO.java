package com.perfumes.nuochoa.dto;



import jakarta.validation.constraints.*;

public class ProductVariantDTO {
    private Long id;
    @NotBlank(message = "SKU không được để trống")
    @Size(max = 50, message = "SKU không được vượt quá 50 ký tự")
    private String sku;
    @NotNull(message = "Vui lòng nhập dung tích")
    @Min(value = 0, message = "Dung tích phải lớn hơn 0")
    private Double volume;
    private Double concentration;
    @NotNull(message = "Vui lòng nhập giá")
    @Min(value = 0, message = "Giá phải lớn hơn 0")
    private Double price;
    @NotNull(message = "Vui lòng nhập tồn kho")
    @Min(value = 0, message = "Tồn kho không được âm")
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
