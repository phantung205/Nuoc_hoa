package com.perfumes.nuochoa.service;

import com.perfumes.nuochoa.entity.Brand;
import java.util.List;

public interface BrandService {
    List<Brand> getAllBrands();        // Lấy tất cả thương hiệu (Admin)
    List<Brand> getActiveBrands();      // Chỉ lấy thương hiệu đang hoạt động (Web Client)
    Brand getBrandById(Long id);
    Brand createBrand(Brand brand, org.springframework.web.multipart.MultipartFile logoFile);
    Brand updateBrand(Long id, Brand brandDetails, org.springframework.web.multipart.MultipartFile logoFile);
    void toggleStatus(Long id);        // Đảo trạng thái hiển thị (Bật / Tắt)
    void deleteBrand(Long id);
}