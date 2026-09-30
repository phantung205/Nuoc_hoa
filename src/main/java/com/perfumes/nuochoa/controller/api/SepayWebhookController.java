package com.perfumes.nuochoa.controller.api;

import com.perfumes.nuochoa.entity.Order;
import com.perfumes.nuochoa.entity.OrderDetail;
import com.perfumes.nuochoa.entity.ProductVariant;
import com.perfumes.nuochoa.repository.OrderDetailRepository;
import com.perfumes.nuochoa.repository.OrderRepository;
import com.perfumes.nuochoa.repository.ProductVariantRepository;
import com.perfumes.nuochoa.service.OrderService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
public class SepayWebhookController {

    private final OrderRepository orderRepository;
    private final OrderService orderService;
    private final OrderDetailRepository orderDetailRepository;
    private final ProductVariantRepository productVariantRepository;

    public SepayWebhookController(OrderRepository orderRepository,
                                  OrderService orderService,
                                  OrderDetailRepository orderDetailRepository,
                                  ProductVariantRepository productVariantRepository) {
        this.orderRepository = orderRepository;
        this.orderService = orderService;
        this.orderDetailRepository = orderDetailRepository;
        this.productVariantRepository = productVariantRepository;
    }

    @PostMapping("/webhook/sepay")
    public ResponseEntity<?> handleSepayWebhook(@RequestBody Map<String, Object> payload) {
        try {
            String content = payload.getOrDefault("content", "").toString().toUpperCase().trim();
            Number amountReceived = (Number) payload.getOrDefault("transferAmount", 0);

            // Tìm orderId từ nội dung chuyển khoản (VD: "LTS123" -> orderId = 123)
            Long orderId = null;
            if (content.contains("LTS")) {
                String orderIdStr = content.substring(content.indexOf("LTS") + 3).replaceAll("[^0-9]", "");
                if (!orderIdStr.isEmpty()) {
                    orderId = Long.parseLong(orderIdStr);
                }
            }

            if (orderId == null) {
                return ResponseEntity.ok(Map.of("success", false, "message", "Không tìm thấy mã đơn hàng trong nội dung CK"));
            }

            Order order = orderRepository.findById(orderId).orElse(null);
            if (order == null) {
                return ResponseEntity.ok(Map.of("success", false, "message", "Đơn hàng không tồn tại"));
            }

            // Kiểm tra số tiền
            double expectedAmount = order.getFinalAmount() != null ? order.getFinalAmount() : order.getTotalAmount();
            if (amountReceived.doubleValue() < expectedAmount) {
                return ResponseEntity.ok(Map.of("success", false, "message", "Số tiền chuyển khoản chưa đủ"));
            }

            // Cập nhật trạng thái đơn hàng sang PAID
            if ("PENDING".equals(order.getStatus()) || "QR".equalsIgnoreCase(order.getPayments())) {
                order.setStatus("PAID");
                order.setPayments("CK");
                orderRepository.save(order);

                // Kiểm tra sản phẩm hết hàng -> ẩn
                checkAndHideOutOfStockProducts(orderId);
            }

            return ResponseEntity.ok(Map.of("success", true, "message", "Xác nhận thanh toán thành công"));
        } catch (Exception e) {
            return ResponseEntity.ok(Map.of("success", false, "message", "Lỗi xử lý: " + e.getMessage()));
        }
    }


    @GetMapping("/api/orders/status/{orderId}")
    public ResponseEntity<?> getOrderStatus(@PathVariable Long orderId) {
        Order order = orderRepository.findById(orderId).orElse(null);
        if (order == null) {
            return ResponseEntity.ok(Map.of("status", "NOT_FOUND"));
        }
        return ResponseEntity.ok(Map.of("status", order.getStatus()));
    }


    private void checkAndHideOutOfStockProducts(Long orderId) {
        List<OrderDetail> details = orderDetailRepository.findByOrderId(orderId);
        for (OrderDetail detail : details) {
            ProductVariant variant = detail.getProductVariant();
            if (variant.getStock() != null && variant.getStock() <= 0) {
                // Ẩn sản phẩm nếu TẤT CẢ variant đều hết hàng
                var product = variant.getProduct();
                List<ProductVariant> allVariants = productVariantRepository.findByProductId(product.getId());
                boolean allOutOfStock = allVariants.stream().allMatch(v -> v.getStock() == null || v.getStock() <= 0);
                if (allOutOfStock) {
                    product.setIsActive(false);
                    // Save qua variant's product reference
                }
            }
        }
    }
}
