package com.perfumes.nuochoa.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import com.perfumes.nuochoa.entity.UserProfile;

public interface UserProfileRepository extends JpaRepository<UserProfile, Long> {
}