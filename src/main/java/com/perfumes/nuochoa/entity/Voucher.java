package com.perfumes.nuochoa.entity;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * Bảng "vouchers" – mã giảm giá dùng khi thanh toán đơn hàng.
 *
 * Hai loại giảm giá (discountType):
 *   - "PERCENT" : giảm theo % tổng đơn hàng (có giới hạn tối đa = maxDiscountAmount)
 *   - "FIXED"   : giảm một số tiền cố định
 *
 * Voucher chỉ hiệu lực khi:
 *   - isActive = true
 *   - Ngày hiện tại nằm trong khoảng [startDate, endDate]
 *   - usageCount < usageLimit (còn lượt dùng)
 *   - Tổng đơn hàng >= minOrderValue
 */
@Entity
@Table(name = "vouchers")
public class Voucher {

    // ===================== FIELDS =====================

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** Mã voucher người dùng nhập (VD: "SALE10"). Không được trùng. */
    @Column(unique = true, nullable = false)
    private String code;

    /** Loại giảm giá: "PERCENT" hoặc "FIXED". */
    @Column(name = "discount_type", nullable = false)
    private String discountType;

    /** Giá trị giảm: nếu PERCENT thì là %, nếu FIXED thì là số tiền VND. */
    @Column(name = "discount_value", nullable = false)
    private BigDecimal discountValue;

    /** Giá trị đơn hàng tối thiểu để áp dụng voucher này. */
    @Column(name = "min_order_value")
    private BigDecimal minOrderValue;

    /** Số tiền giảm tối đa khi dùng PERCENT (null = không giới hạn). */
    @Column(name = "max_discount_amount")
    private BigDecimal maxDiscountAmount;

    /** Tổng số lượt được phép sử dụng. */
    @Column(name = "usage_limit")
    private Integer usageLimit;

    /** Số lượt đã được sử dụng. Tăng lên mỗi khi có đơn hàng dùng voucher này. */
    @Column(name = "usage_count")
    private Integer usageCount = 0;

    @Column(name = "start_date")
    private LocalDate startDate;

    @Column(name = "end_date")
    private LocalDate endDate;

    /** true = voucher đang hoạt động; false = đã vô hiệu hóa. */
    @Column(name = "is_active")
    private Boolean isActive;

    // ===================== GETTERS & SETTERS =====================

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getDiscountType() {
        return discountType;
    }

    public void setDiscountType(String discountType) {
        this.discountType = discountType;
    }

    public BigDecimal getDiscountValue() {
        return discountValue;
    }

    public void setDiscountValue(BigDecimal discountValue) {
        this.discountValue = discountValue;
    }

    public BigDecimal getMinOrderValue() {
        return minOrderValue;
    }

    public void setMinOrderValue(BigDecimal minOrderValue) {
        this.minOrderValue = minOrderValue;
    }

    public BigDecimal getMaxDiscountAmount() {
        return maxDiscountAmount;
    }

    public void setMaxDiscountAmount(BigDecimal maxDiscountAmount) {
        this.maxDiscountAmount = maxDiscountAmount;
    }

    public Integer getUsageLimit() {
        return usageLimit;
    }

    public void setUsageLimit(Integer usageLimit) {
        this.usageLimit = usageLimit;
    }

    public Integer getUsageCount() {
        return usageCount;
    }

    public void setUsageCount(Integer usageCount) {
        this.usageCount = usageCount;
    }

    public LocalDate getStartDate() {
        return startDate;
    }

    public void setStartDate(LocalDate startDate) {
        this.startDate = startDate;
    }

    public LocalDate getEndDate() {
        return endDate;
    }

    public void setEndDate(LocalDate endDate) {
        this.endDate = endDate;
    }

    public Boolean getIsActive() {
        return isActive;
    }

    public void setIsActive(Boolean isActive) {
        this.isActive = isActive;
    }
}