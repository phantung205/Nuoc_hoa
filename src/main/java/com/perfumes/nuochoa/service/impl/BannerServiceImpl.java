package com.perfumes.nuochoa.service.impl;

import com.perfumes.nuochoa.entity.Banner;
import com.perfumes.nuochoa.repository.BannerRepository;
import com.perfumes.nuochoa.service.BannerService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.List;
import java.util.UUID;

@Service
public class BannerServiceImpl implements BannerService {

    private final BannerRepository bannerRepository;

    public BannerServiceImpl(BannerRepository bannerRepository) {
        this.bannerRepository = bannerRepository;
    }

    @Override
    public List<Banner> getAllBanners() {
        return bannerRepository.findAll();
    }

    @Override
    public List<Banner> getActiveBanners() {
        return bannerRepository.findByStatusTrueOrderBySortOrderAsc();
    }

    @Override
    public Banner getBannerById(Long id) {
        return bannerRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy banner với ID: " + id));
    }

    @Override
    @Transactional
    public Banner createBanner(Banner banner, MultipartFile imageFile) {
        if (banner.getStatus() == null) {
            banner.setStatus(true);
        }
        if (banner.getSortOrder() == null) {
            banner.setSortOrder(0);
        }

        if (imageFile != null && !imageFile.isEmpty()) {
            String savedFileName = saveImageFile(imageFile);
            banner.setImageUrl("/uploads/banners/" + savedFileName);
        }

        return bannerRepository.save(banner);
    }

    @Override
    @Transactional
    public Banner updateBanner(Long id, Banner bannerDetails, MultipartFile imageFile) {
        Banner banner = getBannerById(id);
        
        banner.setTitle(bannerDetails.getTitle());
        banner.setDescription(bannerDetails.getDescription());
        banner.setLink(bannerDetails.getLink());
        
        if (bannerDetails.getSortOrder() != null) {
            banner.setSortOrder(bannerDetails.getSortOrder());
        }
        if (bannerDetails.getStatus() != null) {
            banner.setStatus(bannerDetails.getStatus());
        }

        if (imageFile != null && !imageFile.isEmpty()) {
            if (banner.getImageUrl() != null && !banner.getImageUrl().isEmpty()) {
                deleteFile(banner.getImageUrl());
            }
            String savedFileName = saveImageFile(imageFile);
            banner.setImageUrl("/uploads/banners/" + savedFileName);
        } else if (bannerDetails.getImageUrl() != null) {
            banner.setImageUrl(bannerDetails.getImageUrl());
        }

        return bannerRepository.save(banner);
    }

    @Override
    @Transactional
    public void toggleStatus(Long id) {
        Banner banner = getBannerById(id);
        banner.setStatus(!banner.getStatus());
        bannerRepository.save(banner);
    }

    @Override
    @Transactional
    public void deleteBanner(Long id) {
        Banner banner = getBannerById(id);
        if (banner.getImageUrl() != null && !banner.getImageUrl().isEmpty()) {
            deleteFile(banner.getImageUrl());
        }
        bannerRepository.delete(banner);
    }

    private String saveImageFile(MultipartFile file) {
        try {
            String uploadDir = "uploads/banners/";
            File dir = new File(uploadDir);
            if (!dir.exists()) {
                dir.mkdirs();
            }
            String uniqueFileName = UUID.randomUUID() + "_" + file.getOriginalFilename();
            Path targetPath = Paths.get(uploadDir + uniqueFileName);
            Files.copy(file.getInputStream(), targetPath, StandardCopyOption.REPLACE_EXISTING);
            return uniqueFileName;
        } catch (IOException e) {
            throw new RuntimeException("Không thể lưu file ảnh banner!", e);
        }
    }

    private void deleteFile(String fileUrl) {
        try {
            if (fileUrl != null && fileUrl.startsWith("/")) {
                Path path = Paths.get(fileUrl.substring(1));
                Files.deleteIfExists(path);
            }
        } catch (IOException e) {
            System.err.println("Không thể xóa file: " + fileUrl);
        }
    }
}
