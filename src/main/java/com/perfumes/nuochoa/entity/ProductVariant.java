package com.perfumes.nuochoa.entity;

import jakarta.persistence.*;

/**
 * Bảng "product_variants" – các biến thể của một sản phẩm nước hoa.
 *
 * Một sản phẩm có thể có nhiều biến thể khác nhau về dung tích và nồng độ.
 * Ví dụ: Chanel No.5 có 2 biến thể: 50ml EDP và 100ml EDP.
 */
@Entity
@Table(name = "product_variants")
public class ProductVariant {

    // ===================== FIELDS =====================

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** Mã hàng nội bộ (Stock Keeping Unit) – dùng để quản lý kho. */
    private String sku;

    /** Dung tích (đơn vị: ml). VD: 50.00, 100.00. */
    private Double volume;

    /**
     * Nồng độ hương (Concentration).
     * VD: 1 = EDT (Eau de Toilette), 2 = EDP (Eau de Parfum), 3 = Parfum.
     */
    private Double concentration;

    /** Giá bán của biến thể này (đơn vị: VND). */
    private Double price;

    /** Số lượng tồn kho hiện tại. */
    private Integer stock;

    /** Sản phẩm cha mà biến thể này thuộc về. */
    @ManyToOne
    @JoinColumn(name = "product_id", nullable = false)
    private Product product;

    // ===================== GETTERS & SETTERS =====================

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getSku() {
        return sku;
    }

    public void setSku(String sku) {
        this.sku = sku;
    }

    public Double getVolume() {
        return volume;
    }

    public void setVolume(Double volume) {
        this.volume = volume;
    }

    public Double getConcentration() {
        return concentration;
    }

    public void setConcentration(Double concentration) {
        this.concentration = concentration;
    }

    public Double getPrice() {
        return price;
    }

    public void setPrice(Double price) {
        this.price = price;
    }

    public Integer getStock() {
        return stock;
    }

    public void setStock(Integer stock) {
        this.stock = stock;
    }

    public Product getProduct() {
        return product;
    }

    public void setProduct(Product product) {
        this.product = product;
    }
}