package com.perfumes.nuochoa.entity;

import jakarta.persistence.*;

import java.time.LocalDate;


@Entity
@Table(name = "vouchers")
public class Voucher {



    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;


    @Column(unique = true, nullable = false)
    private String code;


    @Column(name = "discount_type", nullable = false)
    private String discountType;


    @Column(name = "discount_value", nullable = false)
    private Double discountValue;


    @Column(name = "min_order_value")
    private Double minOrderValue;


    @Column(name = "max_discount_amount")
    private Double maxDiscountAmount;


    @Column(name = "usage_limit")
    private Integer usageLimit;


    @Column(name = "usage_count")
    private Integer usageCount = 0;

    @Column(name = "start_date")
    @org.springframework.format.annotation.DateTimeFormat(pattern = "yyyy-MM-dd")
    private LocalDate startDate;

    @Column(name = "end_date")
    @org.springframework.format.annotation.DateTimeFormat(pattern = "yyyy-MM-dd")
    private LocalDate endDate;


    @Column(name = "is_active")
    private Boolean isActive;


    @Column(name = "receive_method")
    private String receiveMethod;




    @Column(name = "receive_description", columnDefinition = "TEXT")
    private String receiveDescription;




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

    public Double getDiscountValue() {
        return discountValue;
    }

    public void setDiscountValue(Double discountValue) {
        this.discountValue = discountValue;
    }

    public Double getMinOrderValue() {
        return minOrderValue;
    }

    public void setMinOrderValue(Double minOrderValue) {
        this.minOrderValue = minOrderValue;
    }

    public Double getMaxDiscountAmount() {
        return maxDiscountAmount;
    }

    public void setMaxDiscountAmount(Double maxDiscountAmount) {
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

    public String getReceiveMethod() {
        return receiveMethod;
    }

    public void setReceiveMethod(String receiveMethod) {
        this.receiveMethod = receiveMethod;
    }


    public String getReceiveDescription() {
        return receiveDescription;
    }

    public void setReceiveDescription(String receiveDescription) {
        this.receiveDescription = receiveDescription;
    }

    public Boolean getIsActive() {
        return isActive;
    }

    public void setIsActive(Boolean isActive) {
        this.isActive = isActive;
    }
}

