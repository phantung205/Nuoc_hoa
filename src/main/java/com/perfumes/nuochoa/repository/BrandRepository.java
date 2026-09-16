package com.perfumes.nuochoa.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import com.perfumes.nuochoa.entity.Brand;

public interface BrandRepository extends JpaRepository<Brand, Long> {
    boolean existsByName(String name);
}