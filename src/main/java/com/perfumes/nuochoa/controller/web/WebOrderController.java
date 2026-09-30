package com.perfumes.nuochoa.controller.web;

import com.perfumes.nuochoa.service.OrderService;
import com.perfumes.nuochoa.service.UserService;
import com.perfumes.nuochoa.repository.OrderDetailRepository;
import com.perfumes.nuochoa.entity.Order;
import com.perfumes.nuochoa.entity.OrderDetail;
import com.perfumes.nuochoa.entity.User;
import com.perfumes.nuochoa.entity.UserProfile;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.http.HttpStatus;

import java.util.List;

@Controller
@RequestMapping("/orders")
public class WebOrderController {

    private final OrderService orderService;
    private final UserService userService;
    private final OrderDetailRepository orderDetailRepository;

    public WebOrderController(OrderService orderService, UserService userService, OrderDetailRepository orderDetailRepository) {
        this.orderService = orderService;
        this.userService = userService;
        this.orderDetailRepository = orderDetailRepository;
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

    @GetMapping("/{id}")
    public String viewOrderDetail(@PathVariable Long id, Model model) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String username = auth.getName();
        
        Order order = orderService.getOrderById(id);
        if (order == null || !order.getUser().getUsername().equals(username)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Không tìm thấy đơn hàng");
        }
        
        List<OrderDetail> orderDetails = orderDetailRepository.findByOrderId(id);
        
        User user = userService.findByUsername(username);
        UserProfile userProfile = userService.getUserProfileByUserId(user.getId());
        model.addAttribute("user", user);
        model.addAttribute("userProfile", userProfile);
        
        model.addAttribute("order", order);
        model.addAttribute("orderDetails", orderDetails);
        
        return "web/pages/personal_page/order_detail";
    }
}
