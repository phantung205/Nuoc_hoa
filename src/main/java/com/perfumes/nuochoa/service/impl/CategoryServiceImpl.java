package com.perfumes.nuochoa.service.impl;

import com.perfumes.nuochoa.entity.Category;
import com.perfumes.nuochoa.repository.CategoryRepository;
import com.perfumes.nuochoa.service.CategoryService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class CategoryServiceImpl implements CategoryService {

    private final CategoryRepository categoryRepository;

    public CategoryServiceImpl(CategoryRepository categoryRepository) {
        this.categoryRepository = categoryRepository;
    }

    @Override
    public List<Category> getAllCategories() {
        return categoryRepository.findAll();
    }

    @Override
    public List<Category> getActiveCategories() {
        return categoryRepository.findByIsActiveTrue();
    }

    @Override
    public Category getCategoryById(Long id) {
        return categoryRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy danh mục với ID: " + id));
    }

    @Override
    @Transactional
    public Category createCategory(Category category) {
        if (categoryRepository.existsByName(category.getName().trim())) {
            throw new RuntimeException("Tên danh mục '" + category.getName() + "' đã tồn tại!");
        }
        category.setName(category.getName().trim());

        // Mặc định là active nếu không truyền vào
        if (category.getIsActive() == null) {
            category.setIsActive(true);
        }

        return categoryRepository.save(category);
    }

    @Override
    @Transactional
    public Category updateCategory(Long id, Category categoryDetails) {
        Category category = getCategoryById(id);
        String newName = categoryDetails.getName().trim();

        if (!category.getName().equalsIgnoreCase(newName) && categoryRepository.existsByName(newName)) {
            throw new RuntimeException("Tên danh mục '" + newName + "' đã tồn tại!");
        }

        category.setName(newName);

        // Cập nhật trạng thái active nếu có truyền vào
        if (categoryDetails.getIsActive() != null) {
            category.setIsActive(categoryDetails.getIsActive());
        }

        return categoryRepository.save(category);
    }

    @Override
    @Transactional
    public void toggleStatus(Long id) {
        Category category = getCategoryById(id);
        category.setIsActive(!category.getIsActive()); // Đảo trạng thái true <-> false
        categoryRepository.save(category);
    }

    @Override
    @Transactional
    public void deleteCategory(Long id) {
        Category category = getCategoryById(id);
        categoryRepository.delete(category);
    }
}