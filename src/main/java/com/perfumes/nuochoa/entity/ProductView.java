package com.perfumes.nuochoa.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

/**
 * Bảng "product_views" – lịch sử xem sản phẩm của người dùng.
 *
 * Dùng để gợi ý sản phẩm "Xem gần đây" hoặc phân tích xu hướng sản phẩm phổ biến.
 */
@Entity
@Table(name = "product_views")
public class ProductView {

    // ===================== FIELDS =====================

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** Thời điểm người dùng xem sản phẩm này. */
    @Column(name = "viewed_at")
    private LocalDateTime viewedAt;

    /** Người dùng đã xem sản phẩm. */
    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    /** Sản phẩm được xem. */
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

    public LocalDateTime getViewedAt() {
        return viewedAt;
    }

    public void setViewedAt(LocalDateTime viewedAt) {
        this.viewedAt = viewedAt;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public Product getProduct() {
        return product;
    }

    public void setProduct(Product product) {
        this.product = product;
    }
}