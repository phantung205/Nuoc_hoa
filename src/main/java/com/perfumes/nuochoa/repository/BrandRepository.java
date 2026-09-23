package com.perfumes.nuochoa.repository;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import com.perfumes.nuochoa.entity.Brand;

public interface BrandRepository extends JpaRepository<Brand, Long> {
    boolean existsByName(String name);

    // Lấy danh sách thương hiệu đang hoạt động (dùng cho trang Web Client)
    List<Brand> findByIsActiveTrue();
}