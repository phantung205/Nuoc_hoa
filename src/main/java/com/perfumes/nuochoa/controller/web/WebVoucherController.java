package com.perfumes.nuochoa.controller.web;

import com.perfumes.nuochoa.entity.UserVoucher;
import com.perfumes.nuochoa.entity.Voucher;
import com.perfumes.nuochoa.security.CustomUserDetails;
import com.perfumes.nuochoa.service.BrandService;
import com.perfumes.nuochoa.service.CategoryService;
import com.perfumes.nuochoa.service.VoucherService;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.time.LocalDate;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Controller
@RequestMapping("/vouchers")
public class WebVoucherController {

    private final VoucherService voucherService;
    private final CategoryService categoryService;
    private final BrandService brandService;

    public WebVoucherController(VoucherService voucherService, CategoryService categoryService, BrandService brandService) {
        this.voucherService = voucherService;
        this.categoryService = categoryService;
        this.brandService = brandService;
    }

    @GetMapping
    public String showVoucherPage(Model model, @AuthenticationPrincipal CustomUserDetails currentUser) {
        LocalDate today = LocalDate.now();

        List<Voucher> availableVouchers = voucherService.getAllVouchers().stream()
                .filter(Voucher::getIsActive)
                .filter(v -> v.getStartDate() == null || !v.getStartDate().isAfter(today))
                .filter(v -> v.getEndDate() == null || !v.getEndDate().isBefore(today))
                .filter(v -> v.getUsageLimit() == null || v.getUsageLimit() > v.getUsageCount())
                .collect(Collectors.toList());

        model.addAttribute("vouchers", availableVouchers);

        if (currentUser != null) {
            Long userId = currentUser.getUser().getId();
            List<UserVoucher> userVouchers = voucherService.getUserVouchers(userId);
            List<Long> claimedVoucherIds = userVouchers.stream()
                    .map(uv -> uv.getVoucher().getId())
                    .collect(Collectors.toList());
            model.addAttribute("claimedVoucherIds", claimedVoucherIds);
        } else {
            model.addAttribute("claimedVoucherIds", Collections.emptyList());
        }

        model.addAttribute("categories", categoryService.getActiveCategories());
        model.addAttribute("brands", brandService.getActiveBrands());
        return "web/pages/vouchers/index";
    }

    @PostMapping("/claim/{voucherId}")
    public String claimVoucher(@PathVariable Long voucherId,
                               @AuthenticationPrincipal CustomUserDetails currentUser,
                               RedirectAttributes redirectAttributes) {
        if (currentUser == null) {
            redirectAttributes.addFlashAttribute("errorMessage", "Vui lòng đăng nhập để nhận voucher!");
            return "redirect:/auth/login";
        }
        try {
            voucherService.claimVoucher(voucherId, currentUser.getUser().getUsername());
            redirectAttributes.addFlashAttribute("successMessage", "Nhận voucher thành công!");
        } catch (RuntimeException e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
        }
        return "redirect:/vouchers";
    }
}
