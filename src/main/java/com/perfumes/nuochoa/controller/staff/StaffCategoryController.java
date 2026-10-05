package com.perfumes.nuochoa.controller.staff;

import com.perfumes.nuochoa.entity.Category;
import com.perfumes.nuochoa.service.CategoryService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/staff/categories")
public class StaffCategoryController {

    private final CategoryService categoryService;

    public StaffCategoryController(CategoryService categoryService) {
        this.categoryService = categoryService;
    }

    @GetMapping
    public String listCategories(Model model) {
        model.addAttribute("categories", categoryService.getAllCategories());
        return "staff/pages/categories/list";
    }

    @GetMapping("/add")
    public String showAddForm(Model model) {
        if (!model.containsAttribute("category")) {
            model.addAttribute("category", new Category());
        }
        return "staff/pages/categories/add";
    }

    @PostMapping("/add")
    public String processAddCategory(@ModelAttribute("category") Category category, RedirectAttributes redirectAttributes) {
        try {
            categoryService.createCategory(category);
            return "redirect:/staff/categories?success=created";
        } catch (RuntimeException e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
            redirectAttributes.addFlashAttribute("category", category);
            return "redirect:/staff/categories/add";
        }
    }

    @GetMapping("/edit/{id}")
    public String showEditForm(@PathVariable Long id, Model model) {
        if (!model.containsAttribute("category")) {
            Category category = categoryService.getCategoryById(id);
            model.addAttribute("category", category);
        }
        return "staff/pages/categories/edit";
    }

    @PostMapping("/edit/{id}")
    public String processEditCategory(@PathVariable Long id, @ModelAttribute("category") Category category, RedirectAttributes redirectAttributes) {
        try {
            categoryService.updateCategory(id, category);
            return "redirect:/staff/categories?success=updated";
        } catch (RuntimeException e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
            redirectAttributes.addFlashAttribute("category", category);
            return "redirect:/staff/categories/edit/" + id;
        }
    }

    @GetMapping("/toggle/{id}")
    public String toggleStatus(@PathVariable("id") Long id, RedirectAttributes redirectAttributes) {
        try {
            categoryService.toggleStatus(id);
            return "redirect:/staff/categories?success=updated";
        } catch (RuntimeException e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
            return "redirect:/staff/categories";
        }
    }

    @GetMapping("/delete/{id}")
    public String deleteCategory(@PathVariable("id") Long id, RedirectAttributes redirectAttributes) {
        try {
            categoryService.deleteCategory(id);
            return "redirect:/staff/categories?success=deleted";
        } catch (RuntimeException e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Không thể xóa danh mục này (có thể đang chứa sản phẩm)!");
            return "redirect:/staff/categories";
        }
    }
}
