package com.perfumes.nuochoa.repository;

import com.perfumes.nuochoa.entity.UserVoucher;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface UserVoucherRepository extends JpaRepository<UserVoucher, Long> {
    List<UserVoucher> findByUserId(Long userId);
    boolean existsByUserIdAndVoucherId(Long userId, Long voucherId);
    java.util.Optional<UserVoucher> findByUserIdAndVoucherId(Long userId, Long voucherId);
    
    @org.springframework.transaction.annotation.Transactional
    @org.springframework.data.jpa.repository.Modifying
    @org.springframework.data.jpa.repository.Query("DELETE FROM UserVoucher u WHERE u.voucher.id = :voucherId")
    void deleteByVoucherId(@org.springframework.data.repository.query.Param("voucherId") Long voucherId);
    @org.springframework.transaction.annotation.Transactional
    @org.springframework.data.jpa.repository.Modifying
    @org.springframework.data.jpa.repository.Query("DELETE FROM UserVoucher e WHERE e.user.id = :userId")
    void deleteByUserId(@org.springframework.data.repository.query.Param("userId") Long userId);
}

