package com.perfumes.nuochoa.entity;

import jakarta.persistence.*;

/**
 * Bảng "user_addresses" – địa chỉ giao hàng của người dùng.
 *
 * Một User có thể lưu nhiều địa chỉ, nhưng chỉ 1 địa chỉ có isDefault = true.
 * Địa chỉ mặc định sẽ được tự động chọn khi thanh toán.
 */
@Entity
@Table(name = "user_addresses")
public class UserAddress {

    // ===================== FIELDS =====================

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** Số điện thoại của người nhận hàng. */
    @Column(name = "phone_number")
    private String phoneNumber;

    /** Họ tên đầy đủ của người nhận hàng. */
    @Column(name = "receiver_name")
    private String receiverName;

    /** Địa chỉ giao hàng chi tiết (số nhà, đường, phường, quận, tỉnh). */
    @Column(name = "receiver_address")
    private String receiverAddress;

    /** Ghi chú cho shipper (VD: "Gọi trước khi giao"). */
    @Column(columnDefinition = "TEXT")
    private String note;

    /** true = địa chỉ mặc định; false = địa chỉ phụ. */
    @Column(name = "is_default")
    private Boolean isDefault;

    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    // ===================== GETTERS & SETTERS =====================

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }

    public String getReceiverName() {
        return receiverName;
    }

    public void setReceiverName(String receiverName) {
        this.receiverName = receiverName;
    }

    public String getReceiverAddress() {
        return receiverAddress;
    }

    public void setReceiverAddress(String receiverAddress) {
        this.receiverAddress = receiverAddress;
    }

    public String getNote() {
        return note;
    }

    public void setNote(String note) {
        this.note = note;
    }

    public Boolean getIsDefault() {
        return isDefault;
    }

    public void setIsDefault(Boolean isDefault) {
        this.isDefault = isDefault;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }
}