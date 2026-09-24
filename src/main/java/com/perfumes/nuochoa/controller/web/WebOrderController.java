package com.perfumes.nuochoa.controller.web;

import com.perfumes.nuochoa.service.OrderService;
import com.perfumes.nuochoa.service.UserService;
import com.perfumes.nuochoa.entity.User;
import com.perfumes.nuochoa.entity.UserProfile;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/orders")
public class WebOrderController {

    private final OrderService orderService;
    private final UserService userService;

    public WebOrderController(OrderService orderService, UserService userService) {
        this.orderService = orderService;
        this.userService = userService;
    }

    @GetMapping
    public String viewOrders(Model model) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String username = auth.getName();
        
        User user = userService.findByUsername(username);
        UserProfile userProfile = userService.getUserProfileByUserId(user.getId());
        model.addAttribute("user", user);
        model.addAttribute("userProfile", userProfile);
        model.addAttribute("orders", orderService.getOrdersByUsername(username));
        return "web/pages/personal_page/orders";
    }
}
