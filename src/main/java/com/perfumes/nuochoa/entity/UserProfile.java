package com.perfumes.nuochoa.entity;

import jakarta.persistence.*;
import java.time.LocalDate;

/**
 * Bảng "user_profiles" – thông tin cá nhân mở rộng của người dùng.
 *
 * Quan hệ: 1 User <-> 1 UserProfile (One-to-One, dùng chung khóa chính user_id).
 * Profile được tạo tự động khi User đăng ký tài khoản.
 */
@Entity
@Table(name = "user_profiles")
public class UserProfile {

    // ===================== FIELDS =====================

    /**
     * Khóa chính – lấy từ User.id (shared primary key).
     * @MapsId báo cho JPA biết user_id là khóa ngoại đồng thời là khóa chính.
     */
    @Id
    @Column(name = "user_id")
    private Long userId;

    @OneToOne
    @MapsId
    @JoinColumn(name = "user_id")
    private User user;

    @Column(name = "full_name")
    private String fullName;

    /** Đường dẫn tương đối đến ảnh đại diện (VD: /uploads/avatars/abc.jpg). */
    @Column(name = "avatar_url")
    private String avatarUrl;

    private String phone;

    @Column(name = "date_of_birth")
    private LocalDate dateOfBirth;

    /** Giá trị: "Nam", "Nữ", hoặc "Khác". */
    private String gender;

    /** Điểm tích lũy để đổi voucher giảm giá. Mặc định = 0 khi tạo mới. */
    @Column(name = "loyalty_points")
    private Integer loyaltyPoints = 0;

    // ===================== GETTERS & SETTERS =====================

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public String getFullName() {
        return fullName;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    public String getAvatarUrl() {
        return avatarUrl;
    }

    public void setAvatarUrl(String avatarUrl) {
        this.avatarUrl = avatarUrl;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public LocalDate getDateOfBirth() {
        return dateOfBirth;
    }

    public void setDateOfBirth(LocalDate dateOfBirth) {
        this.dateOfBirth = dateOfBirth;
    }

    public String getGender() {
        return gender;
    }

    public void setGender(String gender) {
        this.gender = gender;
    }

    public Integer getLoyaltyPoints() {
        return loyaltyPoints;
    }

    public void setLoyaltyPoints(Integer loyaltyPoints) {
        this.loyaltyPoints = loyaltyPoints;
    }
}