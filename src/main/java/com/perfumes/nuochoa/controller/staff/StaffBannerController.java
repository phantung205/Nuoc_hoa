package com.perfumes.nuochoa.controller.staff;

import com.perfumes.nuochoa.entity.Banner;
import com.perfumes.nuochoa.service.BannerService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/staff/banners")
public class StaffBannerController {

    private final BannerService bannerService;

    public StaffBannerController(BannerService bannerService) {
        this.bannerService = bannerService;
    }

    @GetMapping
    public String listBanners(Model model) {
        model.addAttribute("banners", bannerService.getAllBanners());
        return "staff/pages/banners/list";
    }

    @GetMapping("/add")
    public String showAddForm(Model model) {
        if (!model.containsAttribute("banner")) {
            model.addAttribute("banner", new Banner());
        }
        return "staff/pages/banners/add";
    }

    @PostMapping("/add")
    public String processAddBanner(@ModelAttribute("banner") Banner banner,
                                   @RequestParam(value = "imageFile", required = false) MultipartFile imageFile,
                                   RedirectAttributes redirectAttributes) {
        try {
            bannerService.createBanner(banner, imageFile);
            return "redirect:/staff/banners?success=created";
        } catch (RuntimeException e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
            redirectAttributes.addFlashAttribute("banner", banner);
            return "redirect:/staff/banners/add";
        }
    }

    @GetMapping("/edit/{id}")
    public String showEditForm(@PathVariable Long id, Model model) {
        if (!model.containsAttribute("banner")) {
            Banner banner = bannerService.getBannerById(id);
            model.addAttribute("banner", banner);
        }
        return "staff/pages/banners/edit";
    }

    @PostMapping("/edit/{id}")
    public String processEditBanner(@PathVariable Long id,
                                    @ModelAttribute("banner") Banner banner,
                                    @RequestParam(value = "imageFile", required = false) MultipartFile imageFile,
                                    RedirectAttributes redirectAttributes) {
        try {
            bannerService.updateBanner(id, banner, imageFile);
            return "redirect:/staff/banners?success=updated";
        } catch (RuntimeException e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
            redirectAttributes.addFlashAttribute("banner", banner);
            return "redirect:/staff/banners/edit/" + id;
        }
    }

    @GetMapping("/toggle/{id}")
    public String toggleStatus(@PathVariable("id") Long id, RedirectAttributes redirectAttributes) {
        try {
            bannerService.toggleStatus(id);
            return "redirect:/staff/banners?success=updated";
        } catch (RuntimeException e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
            return "redirect:/staff/banners";
        }
    }

    @GetMapping("/delete/{id}")
    public String deleteBanner(@PathVariable("id") Long id, RedirectAttributes redirectAttributes) {
        try {
            bannerService.deleteBanner(id);
            return "redirect:/staff/banners?success=deleted";
        } catch (RuntimeException e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Không thể xóa banner này!");
            return "redirect:/staff/banners";
        }
    }
}

