package com.perfumes.nuochoa.service;

import com.perfumes.nuochoa.entity.Banner;
import org.springframework.web.multipart.MultipartFile;
import java.util.List;

public interface BannerService {
    List<Banner> getAllBanners();
    List<Banner> getActiveBanners();
    Banner getBannerById(Long id);
    Banner createBanner(Banner banner, MultipartFile imageFile);
    Banner updateBanner(Long id, Banner bannerDetails, MultipartFile imageFile);
    void toggleStatus(Long id);
    void deleteBanner(Long id);
}
