package com.perfumes.nuochoa.controller.admin;

import com.perfumes.nuochoa.dto.UserAdminRequest;
import com.perfumes.nuochoa.entity.User;
import com.perfumes.nuochoa.service.UserService;
import jakarta.validation.Valid;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

@Controller
@RequestMapping("/admin/users")
public class AdminUserController {

    private final UserService userService;

    public AdminUserController(UserService userService) {
        this.userService = userService;
    }

    // 1. Danh sách người dùng
    @GetMapping
    public String listUsers(Model model) {
        model.addAttribute("users", userService.getAllUsers());
        return "admin/pages/user/list";
    }

    // 2. Giao diện Thêm mới
    @GetMapping("/add")
    public String showAddForm(Model model) {
        UserAdminRequest userRequest = new UserAdminRequest();
        userRequest.setStatus("ACTIVE");
        userRequest.setRole("USER");
        model.addAttribute("userRequest", userRequest);
        return "admin/pages/user/add";
    }

    // 3. Xử lý Thêm mới
    @PostMapping("/add")
    public String processAddUser(@Valid @ModelAttribute("userRequest") UserAdminRequest userRequest,
                                 BindingResult bindingResult, Model model) {
        if (bindingResult.hasErrors()) {
            return "admin/pages/user/add";
        }
        try {
            userService.createUserByAdmin(userRequest);
            return "redirect:/admin/users?success=created";
        } catch (Exception e) {
            model.addAttribute("errorMessage", e.getMessage());
            return "admin/pages/user/add";
        }
    }

    // 4. Giao diện Chỉnh sửa
    @GetMapping("/edit/{id}")
    public String showEditForm(@PathVariable("id") Long id, Model model) {
        User user = userService.getUserById(id);

        UserAdminRequest userRequest = new UserAdminRequest();
        userRequest.setId(user.getId());
        userRequest.setUsername(user.getUsername());
        userRequest.setEmail(user.getEmail());
        userRequest.setStatus(user.getStatus());

        if (user.getRole() != null) {
            userRequest.setRole(user.getRole().getName());
        }

        model.addAttribute("userRequest", userRequest);
        return "admin/pages/user/edit";
    }

    // 5. Xử lý Chỉnh sửa
    @PostMapping("/edit/{id}")
    public String processEditUser(@PathVariable("id") Long id,
                                  @Valid @ModelAttribute("userRequest") UserAdminRequest userRequest,
                                  BindingResult bindingResult, Model model) {
        if (bindingResult.hasErrors()) {
            return "admin/pages/user/edit";
        }
        try {
            userService.updateUserByAdmin(id, userRequest);
            return "redirect:/admin/users?success=updated";
        } catch (Exception e) {
            model.addAttribute("errorMessage", e.getMessage());
            return "admin/pages/user/edit";
        }
    }

    // 6. Nhanh: Khóa / Mở khóa tài khoản
    @PostMapping("/toggle-status/{id}")
    public String toggleUserStatus(@PathVariable("id") Long id) {
        userService.toggleUserStatus(id);
        return "redirect:/admin/users?success=updated";
    }

    // 7. Xử lý Xóa
    @PostMapping("/delete/{id}")
    public String deleteUser(@PathVariable("id") Long id) {
        userService.deleteUser(id);
        return "redirect:/admin/users?success=deleted";
    }
}