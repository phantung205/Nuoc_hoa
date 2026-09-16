package com.perfumes.nuochoa.repository;

import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import com.perfumes.nuochoa.entity.Role;

public interface RoleRepository extends JpaRepository<Role, Long> {
    Optional<Role> findByName(String name);
}