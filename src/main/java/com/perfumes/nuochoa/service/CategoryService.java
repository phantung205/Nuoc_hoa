package com.perfumes.nuochoa.service;

import com.perfumes.nuochoa.entity.Category;
import java.util.List;

public interface CategoryService {
    List<Category> getAllCategories();
    List<Category> getActiveCategories();
    Category getCategoryById(Long id);
    Category createCategory(Category category);
    Category updateCategory(Long id, Category categoryDetails);
    void toggleStatus(Long id);
    void deleteCategory(Long id);
}