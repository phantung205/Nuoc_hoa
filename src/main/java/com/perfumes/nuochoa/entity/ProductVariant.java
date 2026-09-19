package com.perfumes.nuochoa.entity;

import jakarta.persistence.*;
import java.math.BigDecimal;

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
    private BigDecimal volume;

    /**
     * Nồng độ hương (Concentration).
     * VD: 1 = EDT (Eau de Toilette), 2 = EDP (Eau de Parfum), 3 = Parfum.
     */
    private BigDecimal concentration;

    /** Giá bán của biến thể này (đơn vị: VND). */
    private BigDecimal price;

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

    public BigDecimal getVolume() {
        return volume;
    }

    public void setVolume(BigDecimal volume) {
        this.volume = volume;
    }

    public BigDecimal getConcentration() {
        return concentration;
    }

    public void setConcentration(BigDecimal concentration) {
        this.concentration = concentration;
    }

    public BigDecimal getPrice() {
        return price;
    }

    public void setPrice(BigDecimal price) {
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