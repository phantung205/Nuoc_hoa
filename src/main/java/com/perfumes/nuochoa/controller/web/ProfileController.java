package com.perfumes.nuochoa.controller.web;

import com.perfumes.nuochoa.dto.UserProfileRequest;
import com.perfumes.nuochoa.entity.UserProfile;
import com.perfumes.nuochoa.security.CustomUserDetails;
import com.perfumes.nuochoa.service.UserService;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

/**
 * Controller xử lý trang Hồ sơ cá nhân của người dùng đang đăng nhập.
 *
 * @AuthenticationPrincipal CustomUserDetails currentUser
 *   → Spring Security tự động inject người dùng đang đăng nhập vào tham số này,
 *     không cần gọi SecurityContextHolder thủ công.
 */
@Controller
@RequestMapping("/profile")
public class ProfileController {

    private final UserService userService;

    public ProfileController(UserService userService) {
        this.userService = userService;
    }

    /**
     * Hiển thị trang hồ sơ cá nhân.
     * Load UserProfile từ DB và đổ dữ liệu vào form để hiển thị.
     */
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

        return "web/pages/profile";
    }

    /**
     * Xử lý form cập nhật hồ sơ cá nhân.
     * Dùng RedirectAttributes để hiển thị thông báo thành công sau khi redirect.
     */
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
}