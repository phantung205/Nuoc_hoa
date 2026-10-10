package com.perfumes.nuochoa.repository;

import com.perfumes.nuochoa.entity.UserAddress;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

public interface UserAddressRepository extends JpaRepository<UserAddress, Long> {

    List<UserAddress> findByUserId(Long userId);

    @Transactional
    @Modifying
    @Query("DELETE FROM UserAddress e WHERE e.user.id = :userId")
    void deleteByUserId(@Param("userId") Long userId);
}