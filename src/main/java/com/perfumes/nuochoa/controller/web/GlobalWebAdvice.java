package com.perfumes.nuochoa.controller.web;

import com.perfumes.nuochoa.entity.User;
import com.perfumes.nuochoa.entity.UserProfile;
import com.perfumes.nuochoa.entity.Category;
import com.perfumes.nuochoa.service.UserService;
import com.perfumes.nuochoa.service.CategoryService;
import com.perfumes.nuochoa.service.BrandService;
import com.perfumes.nuochoa.entity.Brand;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;

import java.util.List;

@ControllerAdvice(basePackages = "com.perfumes.nuochoa.controller.web")
public class GlobalWebAdvice {

    private final UserService userService;
    private final CategoryService categoryService;
    private final BrandService brandService;

    public GlobalWebAdvice(UserService userService, CategoryService categoryService, BrandService brandService) {
        this.userService = userService;
        this.categoryService = categoryService;
        this.brandService = brandService;
    }

    @ModelAttribute("globalUserProfile")
    public UserProfile populateUserProfile() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.isAuthenticated() && !auth.getName().equals("anonymousUser")) {
            User user = userService.findByUsername(auth.getName());
            if (user != null) {
                return userService.getUserProfileByUserId(user.getId());
            }
        }
        return null;
    }
    
    @ModelAttribute("categories")
    public List<Category> populateCategories() {
        return categoryService.getActiveCategories();
    }

    @ModelAttribute("brands")
    public List<Brand> populateBrands() {
        return brandService.getActiveBrands();
    }
}
