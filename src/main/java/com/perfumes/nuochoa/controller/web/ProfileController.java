package com.perfumes.nuochoa.controller.web;

import com.perfumes.nuochoa.dto.UserProfileRequest;
import com.perfumes.nuochoa.entity.UserProfile;
import com.perfumes.nuochoa.security.CustomUserDetails;
import com.perfumes.nuochoa.service.UserService;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile; // Bổ sung import này để sửa lỗi đỏ
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/profile")
public class ProfileController {

    private final UserService userService;

    public ProfileController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping
    public String showProfile(@AuthenticationPrincipal CustomUserDetails currentUser, Model model) {
        Long userId = currentUser.getUser().getId();
        UserProfile profile = userService.getUserProfileByUserId(userId);

        UserProfileRequest profileRequest = new UserProfileRequest();
        profileRequest.setFullName(profile.getFullName());
        profileRequest.setAvatarUrl(profile.getAvatarUrl());
        profileRequest.setPhone(profile.getPhone());
        profileRequest.setDateOfBirth(profile.getDateOfBirth());
        profileRequest.setGender(profile.getGender());

        model.addAttribute("profileRequest", profileRequest);
        model.addAttribute("userProfile", profile);
        model.addAttribute("user", currentUser.getUser());

        return "web/pages/profile";
    }

    @PostMapping("/update")
    public String updateProfile(@AuthenticationPrincipal CustomUserDetails currentUser,
                                @ModelAttribute("profileRequest") UserProfileRequest profileRequest,
                                @RequestParam(value = "avatarFile", required = false) MultipartFile avatarFile,
                                RedirectAttributes redirectAttributes) {
        Long userId = currentUser.getUser().getId();
        userService.updateUserProfile(userId, profileRequest, avatarFile);
        redirectAttributes.addFlashAttribute("successMessage", "Cập nhật thông tin thành công!");
        return "redirect:/profile";
    }
}