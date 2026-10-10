package com.perfumes.nuochoa.controller.staff;

import com.perfumes.nuochoa.entity.Brand;
import com.perfumes.nuochoa.service.BrandService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/staff/brands")
public class StaffBrandController {

    private final BrandService brandService;

    public StaffBrandController(BrandService brandService) {
        this.brandService = brandService;
    }

    @GetMapping
    public String listBrands(Model model) {
        model.addAttribute("brands", brandService.getAllBrands());
        return "staff/pages/bands/list";
    }

    @GetMapping("/add")
    public String showAddForm(Model model) {
        if (!model.containsAttribute("brand")) {
            model.addAttribute("brand", new Brand());
        }
        return "staff/pages/bands/add";
    }

    @PostMapping("/add")
    public String processAddBrand(@ModelAttribute("brand") Brand brand,
                                  @RequestParam(value = "logoFile", required = false) MultipartFile logoFile,
                                  RedirectAttributes redirectAttributes) {
        try {
            brandService.createBrand(brand, logoFile);
            return "redirect:/staff/brands?success=created";
        } catch (RuntimeException e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
            redirectAttributes.addFlashAttribute("brand", brand);
            return "redirect:/staff/brands/add";
        }
    }

    @GetMapping("/edit/{id}")
    public String showEditForm(@PathVariable Long id, Model model) {
        if (!model.containsAttribute("brand")) {
            Brand brand = brandService.getBrandById(id);
            model.addAttribute("brand", brand);
        }
        return "staff/pages/bands/edit";
    }

    @PostMapping("/edit/{id}")
    public String processEditBrand(@PathVariable Long id,
                                   @ModelAttribute("brand") Brand brand,
                                   @RequestParam(value = "logoFile", required = false) MultipartFile logoFile,
                                   RedirectAttributes redirectAttributes) {
        try {
            brandService.updateBrand(id, brand, logoFile);
            return "redirect:/staff/brands?success=updated";
        } catch (RuntimeException e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
            redirectAttributes.addFlashAttribute("brand", brand);
            return "redirect:/staff/brands/edit/" + id;
        }
    }

    @GetMapping("/toggle/{id}")
    public String toggleStatus(@PathVariable("id") Long id, RedirectAttributes redirectAttributes) {
        try {
            brandService.toggleStatus(id);
            return "redirect:/staff/brands?success=updated";
        } catch (RuntimeException e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
            return "redirect:/staff/brands";
        }
    }

    @GetMapping("/delete/{id}")
    public String deleteBrand(@PathVariable("id") Long id, RedirectAttributes redirectAttributes) {
        try {
            brandService.deleteBrand(id);
            return "redirect:/staff/brands?success=deleted";
        } catch (RuntimeException e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Không thể xóa thương hiệu này!");
            return "redirect:/staff/brands";
        }
    }
}