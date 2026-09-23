package com.perfumes.nuochoa.controller.admin;

import com.perfumes.nuochoa.entity.Brand;
import com.perfumes.nuochoa.service.BrandService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/admin/brands")
public class AdminBrandController {

    private final BrandService brandService;

    public AdminBrandController(BrandService brandService) {
        this.brandService = brandService;
    }

    @GetMapping
    public String listBrands(Model model) {
        model.addAttribute("brands", brandService.getAllBrands());
        return "admin/pages/bands/list";
    }

    @GetMapping("/add")
    public String showAddForm(Model model) {
        model.addAttribute("brand", new Brand());
        return "admin/pages/bands/add";
    }

    @PostMapping("/add")
    public String processAddBrand(@ModelAttribute("brand") Brand brand, 
                                  @RequestParam(value = "logoFile", required = false) org.springframework.web.multipart.MultipartFile logoFile, 
                                  RedirectAttributes redirectAttributes) {
        try {
            brandService.createBrand(brand, logoFile);
            return "redirect:/admin/brands?success=created";
        } catch (RuntimeException e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
            return "redirect:/admin/brands/add";
        }
    }

    @GetMapping("/edit/{id}")
    public String showEditForm(@PathVariable Long id, Model model) {
        Brand brand = brandService.getBrandById(id);
        model.addAttribute("brand", brand);
        return "admin/pages/bands/edit";
    }

    @PostMapping("/edit/{id}")
    public String processEditBrand(@PathVariable Long id, 
                                   @ModelAttribute("brand") Brand brand, 
                                   @RequestParam(value = "logoFile", required = false) org.springframework.web.multipart.MultipartFile logoFile, 
                                   RedirectAttributes redirectAttributes) {
        try {
            brandService.updateBrand(id, brand, logoFile);
            return "redirect:/admin/brands?success=updated";
        } catch (RuntimeException e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
            return "redirect:/admin/brands/edit/" + id;
        }
    }

    @GetMapping("/toggle/{id}")
    public String toggleStatus(@PathVariable("id") Long id, RedirectAttributes redirectAttributes) {
        try {
            brandService.toggleStatus(id);
            return "redirect:/admin/brands?success=updated";
        } catch (RuntimeException e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
            return "redirect:/admin/brands";
        }
    }

    @GetMapping("/delete/{id}")
    public String deleteBrand(@PathVariable("id") Long id, RedirectAttributes redirectAttributes) {
        try {
            brandService.deleteBrand(id);
            return "redirect:/admin/brands?success=deleted";
        } catch (RuntimeException e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Không thể xóa thương hiệu này!");
            return "redirect:/admin/brands";
        }
    }
}