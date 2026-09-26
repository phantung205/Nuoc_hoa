package com.perfumes.nuochoa.repository;

import com.perfumes.nuochoa.entity.Banner;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface BannerRepository extends JpaRepository<Banner, Long> {
    // Lấy danh sách banner đang hiển thị và sắp xếp theo thứ tự
    List<Banner> findByStatusTrueOrderBySortOrderAsc();
}
