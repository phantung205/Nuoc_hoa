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


    @GetMapping
    public String listUsers(Model model) {
        model.addAttribute("users", userService.getAllUsers());
        return "admin/pages/user/list";
    }


    @GetMapping("/add")
    public String showAddForm(Model model) {
        UserAdminRequest userRequest = new UserAdminRequest();
        userRequest.setStatus("ACTIVE");
        userRequest.setRole("USER");
        model.addAttribute("userRequest", userRequest);
        return "admin/pages/user/add";
    }

    @PostMapping("/add")
    public String processAddUser(@Valid @ModelAttribute("userRequest") UserAdminRequest userRequest,
                                 BindingResult bindingResult,
                                 Model model) {
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


    @GetMapping("/edit/{id}")
    public String showEditForm(@PathVariable Long id, Model model) {
        User user = userService.getUserById(id);

        // Chuyển dữ liệu từ entity User sang DTO để hiển thị trên form
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

    @PostMapping("/edit/{id}")
    public String processEditUser(@PathVariable Long id,
                                  @Valid @ModelAttribute("userRequest") UserAdminRequest userRequest,
                                  BindingResult bindingResult,
                                  Model model) {
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

    @PostMapping("/toggle-status/{id}")
    public String toggleUserStatus(@PathVariable Long id) {
        userService.toggleUserStatus(id);
        return "redirect:/admin/users?success=updated";
    }


    @PostMapping("/delete/{id}")
    public String deleteUser(@PathVariable Long id) {
        userService.deleteUser(id);
        return "redirect:/admin/users?success=deleted";
    }
}