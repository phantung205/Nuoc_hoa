package com.perfumes.nuochoa.controller.admin;

import com.perfumes.nuochoa.entity.Voucher;
import com.perfumes.nuochoa.service.VoucherService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/admin/vouchers")
public class AdminVoucherController {

    private final VoucherService voucherService;

    public AdminVoucherController(VoucherService voucherService) {
        this.voucherService = voucherService;
    }

    @GetMapping
    public String listVouchers(Model model) {
        model.addAttribute("vouchers", voucherService.getAllVouchers());
        return "admin/pages/vouchers/list";
    }

    @GetMapping("/add")
    public String showAddForm(Model model) {
        if (!model.containsAttribute("voucher")) {
            model.addAttribute("voucher", new Voucher());
        }
        return "admin/pages/vouchers/add";
    }

    @PostMapping("/add")
    public String processAddVoucher(@ModelAttribute("voucher") Voucher voucher, RedirectAttributes redirectAttributes) {
        try {
            if (voucher.getStartDate() != null && voucher.getEndDate() != null && voucher.getStartDate().isAfter(voucher.getEndDate())) {
                throw new RuntimeException("Ngày bắt đầu phải nhỏ hơn hoặc bằng ngày kết thúc");
            }
            voucherService.createVoucher(voucher);
            redirectAttributes.addFlashAttribute("successMessage", "Thêm mã giảm giá thành công!");
            return "redirect:/admin/vouchers";
        } catch (RuntimeException e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
            redirectAttributes.addFlashAttribute("voucher", voucher);
            return "redirect:/admin/vouchers/add";
        }
    }

    @GetMapping("/edit/{id}")
    public String showEditForm(@PathVariable Long id, Model model, RedirectAttributes redirectAttributes) {
        try {
            if (!model.containsAttribute("voucher")) {
                Voucher voucher = voucherService.getVoucherById(id);
                model.addAttribute("voucher", voucher);
            }
            return "admin/pages/vouchers/edit";
        } catch (RuntimeException e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
            return "redirect:/admin/vouchers";
        }
    }

    @PostMapping("/edit/{id}")
    public String processEditVoucher(@PathVariable Long id, @ModelAttribute("voucher") Voucher voucher, RedirectAttributes redirectAttributes) {
        try {
            if (voucher.getStartDate() != null && voucher.getEndDate() != null && voucher.getStartDate().isAfter(voucher.getEndDate())) {
                throw new RuntimeException("Ngày bắt đầu phải nhỏ hơn hoặc bằng ngày kết thúc");
            }
            voucherService.updateVoucher(id, voucher);
            redirectAttributes.addFlashAttribute("successMessage", "Cập nhật mã giảm giá thành công!");
        } catch (RuntimeException e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
            redirectAttributes.addFlashAttribute("voucher", voucher);
            return "redirect:/admin/vouchers/edit/" + id;
        }
        return "redirect:/admin/vouchers";
    }

    @GetMapping("/toggle/{id}")
    public String toggleStatus(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            voucherService.toggleStatus(id);
            redirectAttributes.addFlashAttribute("successMessage", "C?p nh?t tr?ng th�i voucher th�nh c�ng!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "L?i: " + e.getMessage());
        }
        return "redirect:/admin/vouchers";
    }

    @GetMapping("/delete/{id}")
    public String deleteVoucher(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            voucherService.deleteVoucher(id);
            redirectAttributes.addFlashAttribute("successMessage", "Xóa mã giảm giá thành công!");
        } catch (RuntimeException e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
        }
        return "redirect:/admin/vouchers";
    }
}

