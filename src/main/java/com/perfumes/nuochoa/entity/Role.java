package com.perfumes.nuochoa.entity;

import jakarta.persistence.*;

/**
 * Bảng "roles" – danh sách vai trò / quyền hạn trong hệ thống.
 *
 * Hiện tại có 2 role:
 *   - USER  : người dùng thông thường
 *   - ADMIN : quản trị viên
 *
 * Lưu ý: Spring Security tự động thêm tiền tố "ROLE_" khi so sánh quyền.
 * Ví dụ: role "ADMIN" trong DB → "ROLE_ADMIN" trong Spring Security.
 */
@Entity
@Table(name = "roles")
public class Role {

    // ===================== FIELDS =====================

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** Tên role (VD: "USER", "ADMIN"). Không được trùng lặp. */
    @Column(unique = true, nullable = false, length = 50)
    private String name;

    @Column(columnDefinition = "TEXT")
    private String description;

    // ===================== GETTERS & SETTERS =====================

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }
}