package com.perfumes.nuochoa.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import com.perfumes.nuochoa.entity.Category;

public interface CategoryRepository extends JpaRepository<Category, Long> {
    boolean existsByName(String name);
}