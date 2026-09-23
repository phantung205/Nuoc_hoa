package com.perfumes.nuochoa.service;

import com.perfumes.nuochoa.entity.Category;
import java.util.List;

public interface CategoryService {
    List<Category> getAllCategories();       // Dành cho Admin (lấy tất cả)
    List<Category> getActiveCategories();     // Dành cho Web Client (chỉ lấy loại active)
    Category getCategoryById(Long id);
    Category createCategory(Category category);
    Category updateCategory(Long id, Category categoryDetails);
    void toggleStatus(Long id);               // Bật/tắt trạng thái hiển thị nhanh
    void deleteCategory(Long id);
}