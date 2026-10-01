package com.perfumes.nuochoa.controller.admin;

import com.perfumes.nuochoa.service.DashboardService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

@Controller
@RequestMapping("/admin")
public class AdminController {

    private final DashboardService dashboardService;

    public AdminController(DashboardService dashboardService) {
        this.dashboardService = dashboardService;
    }

    @GetMapping({"", "/", "/dashboard"})
    public String dashboard(Model model) {
        // Tổng quan cards
        model.addAttribute("totalUsers", dashboardService.countTotalUsers());
        model.addAttribute("activeUsers", dashboardService.countActiveUsers());
        model.addAttribute("totalProducts", dashboardService.countTotalProducts());
        model.addAttribute("activeProducts", dashboardService.countActiveProducts());
        model.addAttribute("totalOrders", dashboardService.countTotalOrders());
        model.addAttribute("totalRevenue", dashboardService.getTotalRevenue());
        model.addAttribute("totalBrands", dashboardService.countTotalBrands());
        model.addAttribute("totalCategories", dashboardService.countTotalCategories());
        model.addAttribute("totalVouchers", dashboardService.countTotalVouchers());

        // Thống kê hôm nay
        model.addAttribute("newUsersToday", dashboardService.countNewUsersToday());
        model.addAttribute("newOrdersToday", dashboardService.countNewOrdersToday());
        model.addAttribute("revenueToday", dashboardService.getRevenueToday());

        // Thống kê tháng này
        model.addAttribute("newOrdersThisMonth", dashboardService.countNewOrdersThisMonth());
        model.addAttribute("revenueThisMonth", dashboardService.getRevenueThisMonth());

        // Đơn hàng theo trạng thái
        model.addAttribute("pendingOrders", dashboardService.countOrdersByStatus("PENDING"));
        model.addAttribute("confirmedOrders", dashboardService.countOrdersByStatus("CONFIRMED"));
        model.addAttribute("shippingOrders", dashboardService.countOrdersByStatus("SHIPPING"));
        model.addAttribute("deliveredOrders", dashboardService.countOrdersByStatus("DELIVERED"));
        model.addAttribute("cancelledOrders", dashboardService.countOrdersByStatus("CANCELLED"));

        // Đơn hàng gần đây
        model.addAttribute("recentOrders", dashboardService.getRecentOrders());

        // Top sản phẩm bán chạy
        model.addAttribute("topProducts", dashboardService.getTopSellingProducts(5));

        return "admin/dashboard";
    }

    @GetMapping("/reports")
    public String reports(Model model) {
        // Tổng quan
        model.addAttribute("totalUsers", dashboardService.countTotalUsers());
        model.addAttribute("totalProducts", dashboardService.countTotalProducts());
        model.addAttribute("totalOrders", dashboardService.countTotalOrders());
        model.addAttribute("totalRevenue", dashboardService.getTotalRevenue());
        model.addAttribute("revenueThisMonth", dashboardService.getRevenueThisMonth());
        model.addAttribute("newOrdersThisMonth", dashboardService.countNewOrdersThisMonth());

        // Đơn hàng theo trạng thái
        model.addAttribute("orderCountByStatus", dashboardService.getOrderCountByStatus());

        // Sản phẩm theo danh mục
        model.addAttribute("productCountByCategory", dashboardService.getProductCountByCategory());

        // Top sản phẩm bán chạy
        model.addAttribute("topProducts", dashboardService.getTopSellingProducts(10));

        // Doanh thu 7 ngày gần đây
        model.addAttribute("revenueLast7Days", dashboardService.getRevenueLast7Days());

        // Doanh thu 12 tháng gần đây
        model.addAttribute("revenueLast12Months", dashboardService.getRevenueLast12Months());

        // Năm hiện tại để filter
        model.addAttribute("currentYear", LocalDate.now().getYear());

        return "admin/pages/reports/reports";
    }

    // API endpoint cho AJAX - lấy doanh thu theo năm
    @GetMapping("/api/revenue-by-year")
    @ResponseBody
    public List<Map<String, Object>> getRevenueByYear(@RequestParam int year) {
        return dashboardService.getRevenueByYear(year);
    }
}