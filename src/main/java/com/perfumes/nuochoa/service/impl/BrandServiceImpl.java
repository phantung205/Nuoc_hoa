package com.perfumes.nuochoa.service.impl;

import com.perfumes.nuochoa.entity.Brand;
import com.perfumes.nuochoa.repository.BrandRepository;
import com.perfumes.nuochoa.service.BrandService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class BrandServiceImpl implements BrandService {

    private final BrandRepository brandRepository;

    public BrandServiceImpl(BrandRepository brandRepository) {
        this.brandRepository = brandRepository;
    }

    @Override
    public List<Brand> getAllBrands() {
        return brandRepository.findAll();
    }

    @Override
    public List<Brand> getActiveBrands() {
        return brandRepository.findByIsActiveTrue();
    }

    @Override
    public Brand getBrandById(Long id) {
        return brandRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy thương hiệu với ID: " + id));
    }

    @Override
    @Transactional
    public Brand createBrand(Brand brand, org.springframework.web.multipart.MultipartFile logoFile) {
        // Kiểm tra trùng tên thương hiệu
        if (brandRepository.existsByName(brand.getName().trim())) {
            throw new RuntimeException("Tên thương hiệu '" + brand.getName() + "' đã tồn tại!");
        }

        brand.setName(brand.getName().trim());

        // Mặc định cho phép hoạt động nếu không truyền vào
        if (brand.getIsActive() == null) {
            brand.setIsActive(true);
        }

        if (logoFile != null && !logoFile.isEmpty()) {
            String savedFileName = saveLogoFile(logoFile);
            brand.setLogo("/uploads/brands/" + savedFileName);
        }

        return brandRepository.save(brand);
    }

    @Override
    @Transactional
    public Brand updateBrand(Long id, Brand brandDetails, org.springframework.web.multipart.MultipartFile logoFile) {
        Brand brand = getBrandById(id);
        String newName = brandDetails.getName().trim();

        // Kiểm tra tên trùng nếu có sự thay đổi tên
        if (!brand.getName().equalsIgnoreCase(newName) && brandRepository.existsByName(newName)) {
            throw new RuntimeException("Tên thương hiệu '" + newName + "' đã tồn tại!");
        }

        brand.setName(newName);
        brand.setOriginCountry(brandDetails.getOriginCountry());
        
        if (logoFile != null && !logoFile.isEmpty()) {
            String savedFileName = saveLogoFile(logoFile);
            brand.setLogo("/uploads/brands/" + savedFileName);
        } else if (brandDetails.getLogo() != null) {
            brand.setLogo(brandDetails.getLogo());
        }

        if (brandDetails.getIsActive() != null) {
            brand.setIsActive(brandDetails.getIsActive());
        }

        return brandRepository.save(brand);
    }

    private String saveLogoFile(org.springframework.web.multipart.MultipartFile file) {
        try {
            String uploadDir = "uploads/brands/";

            // Tạo thư mục nếu chưa tồn tại
            java.io.File dir = new java.io.File(uploadDir);
            if (!dir.exists()) {
                dir.mkdirs();
            }
            // Tạo tên file duy nhất
            String uniqueFileName = java.util.UUID.randomUUID() + "_" + file.getOriginalFilename();
            java.nio.file.Path targetPath = java.nio.file.Paths.get(uploadDir + uniqueFileName);

            java.nio.file.Files.copy(file.getInputStream(), targetPath, java.nio.file.StandardCopyOption.REPLACE_EXISTING);
            return uniqueFileName;

        } catch (java.io.IOException e) {
            throw new RuntimeException("Không thể lưu file ảnh logo!", e);
        }
    }

    @Override
    @Transactional
    public void toggleStatus(Long id) {
        Brand brand = getBrandById(id);
        brand.setIsActive(!brand.getIsActive()); // Bật <-> Tắt
        brandRepository.save(brand);
    }

    @Override
    @Transactional
    public void deleteBrand(Long id) {
        Brand brand = getBrandById(id);
        brandRepository.delete(brand);
    }
}