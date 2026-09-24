package com.perfumes.nuochoa.controller.web;

import com.perfumes.nuochoa.dto.UserAddressDTO;
import com.perfumes.nuochoa.entity.User;
import com.perfumes.nuochoa.entity.UserProfile;
import com.perfumes.nuochoa.service.UserAddressService;
import com.perfumes.nuochoa.service.UserService;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/profile/addresses")
public class WebUserAddressController {

    private final UserAddressService userAddressService;
    private final UserService userService;

    public WebUserAddressController(UserAddressService userAddressService, UserService userService) {
        this.userAddressService = userAddressService;
        this.userService = userService;
    }

    @GetMapping
    public String viewAddresses(Model model) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String username = auth.getName();
        
        User user = userService.findByUsername(username);
        UserProfile userProfile = userService.getUserProfileByUserId(user.getId());
        
        model.addAttribute("user", user);
        model.addAttribute("userProfile", userProfile);
        model.addAttribute("addresses", userAddressService.getUserAddresses(username));
        return "web/pages/personal_page/addresses";
    }

    @PostMapping("/add")
    public String addAddress(@ModelAttribute UserAddressDTO dto, RedirectAttributes redirectAttributes) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        try {
            userAddressService.addAddress(auth.getName(), dto);
            redirectAttributes.addFlashAttribute("successMessage", "Thêm địa chỉ thành công!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
        }
        return "redirect:/profile/addresses";
    }

    @PostMapping("/update/{id}")
    public String updateAddress(@PathVariable Long id, @ModelAttribute UserAddressDTO dto, RedirectAttributes redirectAttributes) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        try {
            userAddressService.updateAddress(auth.getName(), id, dto);
            redirectAttributes.addFlashAttribute("successMessage", "Cập nhật địa chỉ thành công!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
        }
        return "redirect:/profile/addresses";
    }

    @PostMapping("/delete/{id}")
    public String deleteAddress(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        try {
            userAddressService.deleteAddress(auth.getName(), id);
            redirectAttributes.addFlashAttribute("successMessage", "Xóa địa chỉ thành công!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
        }
        return "redirect:/personal_page/addresses";
    }

    @PostMapping("/set-default/{id}")
    public String setDefaultAddress(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        try {
            userAddressService.setDefaultAddress(auth.getName(), id);
            redirectAttributes.addFlashAttribute("successMessage", "Đã đặt làm địa chỉ mặc định!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
        }
        return "redirect:/personal_page/addresses";
    }
}
