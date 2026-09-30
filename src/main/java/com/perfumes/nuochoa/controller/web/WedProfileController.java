package com.perfumes.nuochoa.controller.web;

import com.perfumes.nuochoa.dto.UserProfileRequest;
import com.perfumes.nuochoa.entity.UserProfile;
import com.perfumes.nuochoa.security.CustomUserDetails;
import com.perfumes.nuochoa.service.UserService;
import com.perfumes.nuochoa.service.VoucherService;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;


@Controller
@RequestMapping("/profile")
public class WedProfileController {

    private final UserService userService;
    private final VoucherService voucherService;

    public WedProfileController(UserService userService, VoucherService voucherService) {
        this.userService = userService;
        this.voucherService = voucherService;
    }

    @GetMapping
    public String showProfile(@AuthenticationPrincipal CustomUserDetails currentUser, Model model) {
        Long userId = currentUser.getUser().getId();
        UserProfile profile = userService.getUserProfileByUserId(userId);

        // Tạo DTO chứa dữ liệu hiện tại để Thymeleaf bind vào form
        UserProfileRequest profileRequest = new UserProfileRequest();
        profileRequest.setFullName(profile.getFullName());
        profileRequest.setAvatarUrl(profile.getAvatarUrl());
        profileRequest.setPhone(profile.getPhone());
        profileRequest.setDateOfBirth(profile.getDateOfBirth());
        profileRequest.setGender(profile.getGender());

        model.addAttribute("profileRequest", profileRequest);
        model.addAttribute("userProfile", profile);
        model.addAttribute("user", currentUser.getUser());
        model.addAttribute("userVouchers", voucherService.getUserVouchers(userId));

        return "web/pages/personal_page/profile";
    }


    @PostMapping("/update")
    public String updateProfile(@AuthenticationPrincipal CustomUserDetails currentUser,
                                @ModelAttribute("profileRequest") UserProfileRequest profileRequest,
                                @RequestParam(value = "avatarFile", required = false) MultipartFile avatarFile,
                                RedirectAttributes redirectAttributes) {
        Long userId = currentUser.getUser().getId();
        userService.updateUserProfile(userId, profileRequest, avatarFile);

        // Flash attribute: tồn tại qua 1 lần redirect rồi tự xóa
        redirectAttributes.addFlashAttribute("successMessage", "Cập nhật thông tin thành công!");
        return "redirect:/profile";
    }


    @GetMapping("/vouchers")
    public String showVouchers(@AuthenticationPrincipal CustomUserDetails currentUser, Model model) {
        Long userId = currentUser.getUser().getId();
        model.addAttribute("userVouchers", voucherService.getUserVouchers(userId));
        model.addAttribute("user", currentUser.getUser());
        return "web/pages/personal_page/vouchers";
    }

}