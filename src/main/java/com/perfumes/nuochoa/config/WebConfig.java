package com.perfumes.nuochoa.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * Cấu hình Spring MVC cho các tài nguyên tĩnh bổ sung.
 *
 * Mặc định Spring Boot chỉ phục vụ file từ thư mục src/main/resources/static/.
 * Class này bổ sung thêm thư mục "uploads/" nằm ở thư mục gốc của project
 * để phục vụ ảnh đại diện người dùng và ảnh sản phẩm đã upload.
 *
 * Kết quả: URL /uploads/avatars/abc.jpg → file uploads/avatars/abc.jpg trên server.
 */
@Configuration
public class WebConfig implements WebMvcConfigurer {

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        // Lấy đường dẫn tuyệt đối của thư mục "uploads" ở gốc project
        Path uploadDir = Paths.get("uploads");
        String absoluteUploadPath = uploadDir.toFile().getAbsolutePath();

        // Map URL /uploads/** → thư mục uploads/ trên đĩa cứng
        registry.addResourceHandler("/uploads/**")
                .addResourceLocations("file:" + absoluteUploadPath + "/");
    }
}