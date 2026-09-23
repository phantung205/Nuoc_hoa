package com.perfumes.nuochoa.controller.admin;

import com.perfumes.nuochoa.entity.Category;
import com.perfumes.nuochoa.service.CategoryService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/admin/categories")
public class AdminCategoryController {

    private final CategoryService categoryService;

    public AdminCategoryController(CategoryService categoryService) {
        this.categoryService = categoryService;
    }

    @GetMapping
    public String listCategories(Model model) {
        model.addAttribute("categories", categoryService.getAllCategories());
        return "admin/pages/categories/list";
    }

    @GetMapping("/add")
    public String showAddForm(Model model) {
        model.addAttribute("category", new Category());
        return "admin/pages/categories/add";
    }

    @PostMapping("/add")
    public String processAddCategory(@ModelAttribute("category") Category category, RedirectAttributes redirectAttributes) {
        try {
            categoryService.createCategory(category);
            return "redirect:/admin/categories?success=created";
        } catch (RuntimeException e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
            return "redirect:/admin/categories/add";
        }
    }

    @GetMapping("/edit/{id}")
    public String showEditForm(@PathVariable Long id, Model model) {
        Category category = categoryService.getCategoryById(id);
        model.addAttribute("category", category);
        return "admin/pages/categories/edit";
    }

    @PostMapping("/edit/{id}")
    public String processEditCategory(@PathVariable Long id, @ModelAttribute("category") Category category, RedirectAttributes redirectAttributes) {
        try {
            categoryService.updateCategory(id, category);
            return "redirect:/admin/categories?success=updated";
        } catch (RuntimeException e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
            return "redirect:/admin/categories/edit/" + id;
        }
    }

    @GetMapping("/toggle/{id}")
    public String toggleStatus(@PathVariable("id") Long id, RedirectAttributes redirectAttributes) {
        try {
            categoryService.toggleStatus(id);
            return "redirect:/admin/categories?success=updated";
        } catch (RuntimeException e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
            return "redirect:/admin/categories";
        }
    }

    @GetMapping("/delete/{id}")
    public String deleteCategory(@PathVariable("id") Long id, RedirectAttributes redirectAttributes) {
        try {
            categoryService.deleteCategory(id);
            return "redirect:/admin/categories?success=deleted";
        } catch (RuntimeException e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Không thể xóa danh mục này (có thể đang chứa sản phẩm)!");
            return "redirect:/admin/categories";
        }
    }
}