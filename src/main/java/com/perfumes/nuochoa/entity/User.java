package com.perfumes.nuochoa.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

/**
 * Bảng "users" – tài khoản đăng nhập của người dùng.
 *
 * Mỗi User liên kết tới:
 *   - Role       : phân quyền (USER hoặc ADMIN)
 *   - UserProfile: thông tin cá nhân (họ tên, ảnh, ngày sinh...)
 *   - Cart       : giỏ hàng, được tạo tự động khi đăng ký
 */
@Entity
@Table(name = "users")
public class User {

    // ===================== FIELDS =====================

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false, length = 50)
    private String username;

    @Column(unique = true, nullable = false)
    private String email;

    /**
     * Mật khẩu đã mã hóa bằng BCrypt.
     * Không bao giờ lưu mật khẩu dạng plain text.
     */
    @Column(name = "password_hash", nullable = false)
    private String passwordHash;

    /**
     * Trạng thái tài khoản:
     *   UNVERIFIED – mới đăng ký, chưa xác thực email
     *   ACTIVE     – đang hoạt động bình thường
     *   LOCKED     – bị Admin khóa tạm thời
     */
    @Column(name = "status", length = 20, nullable = false)
    private String status;

    /** Quyền hạn của tài khoản (USER hoặc ADMIN). */
    @ManyToOne
    @JoinColumn(name = "role_id", nullable = false)
    private Role role;

    /** Thời điểm tạo tài khoản – tự động gán, không thay đổi sau đó. */
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    /** Thời điểm cập nhật tài khoản gần nhất – tự động cập nhật. */
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    // ===================== JPA LIFECYCLE =====================

    /** Tự động gán thời gian khi tạo bản ghi lần đầu. */
    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }

    /** Tự động cập nhật thời gian mỗi khi bản ghi được sửa. */
    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = LocalDateTime.now();
    }

    // ===================== GETTERS & SETTERS =====================

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPasswordHash() {
        return passwordHash;
    }

    public void setPasswordHash(String passwordHash) {
        this.passwordHash = passwordHash;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public Role getRole() {
        return role;
    }

    public void setRole(Role role) {
        this.role = role;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }
}