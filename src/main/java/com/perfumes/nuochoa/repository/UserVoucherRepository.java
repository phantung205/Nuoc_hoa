package com.perfumes.nuochoa.repository;

import com.perfumes.nuochoa.entity.UserVoucher;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

public interface UserVoucherRepository extends JpaRepository<UserVoucher, Long> {

    List<UserVoucher> findByUserId(Long userId);

    boolean existsByUserIdAndVoucherId(Long userId, Long voucherId);

    Optional<UserVoucher> findByUserIdAndVoucherId(Long userId, Long voucherId);

    @Transactional
    @Modifying
    @Query("DELETE FROM UserVoucher u WHERE u.voucher.id = :voucherId")
    void deleteByVoucherId(@Param("voucherId") Long voucherId);

    @Transactional
    @Modifying
    @Query("DELETE FROM UserVoucher e WHERE e.user.id = :userId")
    void deleteByUserId(@Param("userId") Long userId);
}