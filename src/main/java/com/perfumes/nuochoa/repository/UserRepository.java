package com.perfumes.nuochoa.repository;

import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import com.perfumes.nuochoa.entity.User;

public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByUsername(String username);
    Optional<User> findByEmail(String email);
    boolean existsByUsername(String username);
    boolean existsByEmail(String email);
}