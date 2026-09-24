package com.perfumes.nuochoa.controller.admin;

import com.perfumes.nuochoa.entity.Order;
import com.perfumes.nuochoa.entity.OrderDetail;
import com.perfumes.nuochoa.repository.OrderDetailRepository;
import com.perfumes.nuochoa.service.OrderService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;

@Controller
@RequestMapping("/admin/orders")
public class AdminOrderController {

    private final OrderService orderService;
    private final OrderDetailRepository orderDetailRepository;

    public AdminOrderController(OrderService orderService, OrderDetailRepository orderDetailRepository) {
        this.orderService = orderService;
        this.orderDetailRepository = orderDetailRepository;
    }

    // 1. Xem danh sách đơn hàng
    @GetMapping
    public String listOrders(Model model) {
        List<Order> orders = orderService.getAllOrders();
        // Sắp xếp đơn hàng mới nhất lên đầu, xử lý trường hợp createdAt bị null
        orders.sort((o1, o2) -> {
            if (o1.getCreatedAt() == null && o2.getCreatedAt() == null) return 0;
            if (o1.getCreatedAt() == null) return 1;
            if (o2.getCreatedAt() == null) return -1;
            return o2.getCreatedAt().compareTo(o1.getCreatedAt());
        });
        model.addAttribute("orders", orders);
        return "admin/pages/orders/list";
    }

    // 2. Xem chi tiết và sửa trạng thái đơn hàng
    @GetMapping("/edit/{id}")
    public String editOrder(@PathVariable Long id, Model model, RedirectAttributes redirectAttributes) {
        try {
            Order order = orderService.getOrderById(id);
            List<OrderDetail> details = orderDetailRepository.findByOrderId(id);
            
            model.addAttribute("order", order);
            model.addAttribute("details", details);
            return "admin/pages/orders/edit";
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Không tìm thấy đơn hàng!");
            return "redirect:/admin/orders";
        }
    }

    // Xử lý cập nhật trạng thái đơn hàng
    @PostMapping("/update/{id}")
    public String updateOrder(@PathVariable Long id, @RequestParam String status, RedirectAttributes redirectAttributes) {
        try {
            orderService.updateOrderStatus(id, status);
            redirectAttributes.addFlashAttribute("successMessage", "Cập nhật trạng thái đơn hàng thành công!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Cập nhật thất bại: " + e.getMessage());
        }
        return "redirect:/admin/orders/edit/" + id;
    }

    // 3. Xóa đơn hàng
    @PostMapping("/delete/{id}")
    public String deleteOrder(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            orderService.deleteOrder(id);
            redirectAttributes.addFlashAttribute("successMessage", "Xóa đơn hàng thành công!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Xóa đơn hàng thất bại: " + e.getMessage());
        }
        return "redirect:/admin/orders";
    }
}
