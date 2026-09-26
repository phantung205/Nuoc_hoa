package com.perfumes.nuochoa.service.impl;

import com.perfumes.nuochoa.entity.*;
import com.perfumes.nuochoa.repository.*;
import com.perfumes.nuochoa.service.CartService;
import com.perfumes.nuochoa.service.OrderService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class OrderServiceImpl implements OrderService {

    private final OrderRepository orderRepository;
    private final OrderDetailRepository orderDetailRepository;
    private final UserRepository userRepository;
    private final CartService cartService;
    private final ProductVariantRepository variantRepository;
    private final UserProfileRepository userProfileRepository;
    private final PointTransactionRepository pointTransactionRepository;

    public OrderServiceImpl(OrderRepository orderRepository,
                            OrderDetailRepository orderDetailRepository,
                            UserRepository userRepository,
                            CartService cartService,
                            ProductVariantRepository variantRepository,
                            UserProfileRepository userProfileRepository,
                            PointTransactionRepository pointTransactionRepository) {
        this.orderRepository = orderRepository;
        this.orderDetailRepository = orderDetailRepository;
        this.userRepository = userRepository;
        this.cartService = cartService;
        this.variantRepository = variantRepository;
        this.userProfileRepository = userProfileRepository;
        this.pointTransactionRepository = pointTransactionRepository;
    }

    @Override
    @Transactional
    public Order createOrder(String username, UserAddress shippingAddress, String paymentMethod, String note, Integer pointsToUse) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("KhÃ´ng tÃ¬m tháº¥y ngÆ°á»i dÃ¹ng"));

        List<CartItem> cartItems = cartService.getCartItemsByUsername(username);
        if (cartItems.isEmpty()) {
            throw new RuntimeException("Giá» hÃ ng Ä‘ang trá»‘ng");
        }

        Double totalAmount = 0.0;
        for (CartItem item : cartItems) {
            ProductVariant variant = item.getProductVariant();
            if (variant.getStock() < item.getQuantity()) {
                throw new RuntimeException("Sáº£n pháº©m " + variant.getProduct().getName() + " khÃ´ng Ä‘á»§ sá»‘ lÆ°á»£ng trong kho");
            }
            variant.setStock(variant.getStock() - item.getQuantity());
            variantRepository.save(variant);
            
            totalAmount += variant.getPrice() * item.getQuantity();
        }

                Double discountAmount = 0.0;
        if (pointsToUse != null && pointsToUse > 0) {
            UserProfile profile = userProfileRepository.findById(user.getId()).orElse(null);
            if (profile == null || profile.getLoyaltyPoints() == null || profile.getLoyaltyPoints() < pointsToUse) {
                throw new RuntimeException("Không đủ điểm tích lũy");
            }
            if (pointsToUse % 100 != 0) {
                throw new RuntimeException("Số điểm sử dụng phải là bội số của 100");
            }
            discountAmount = (pointsToUse / 100) * 10000.0;
            if (discountAmount > totalAmount) {
                discountAmount = totalAmount; // Không giảm quá tổng tiền
            }
            
            profile.setLoyaltyPoints(profile.getLoyaltyPoints() - pointsToUse);
            userProfileRepository.save(profile);
            
            PointTransaction pt = new PointTransaction();
            pt.setUser(user);
            pt.setPoints(-pointsToUse);
            pt.setTransactionType("SPEND_ON_ORDER");
            pointTransactionRepository.save(pt);
        }

        Order order = new Order();
        order.setUser(user);
        order.setShippingAddress(shippingAddress);
        order.setPayments(paymentMethod);
        order.setNote(note);
        order.setTotalAmount(totalAmount);
        order.setDiscountAmount(discountAmount);
        order.setFinalAmount(totalAmount - discountAmount);
        order.setStatus("PENDING");
        order.setCreatedAt(java.time.LocalDateTime.now());

        Order savedOrder = orderRepository.save(order);

        for (CartItem item : cartItems) {
            OrderDetail detail = new OrderDetail();
            detail.setOrder(savedOrder);
            detail.setProductVariant(item.getProductVariant());
            detail.setQuantity(item.getQuantity());
            detail.setUnitPrice(item.getProductVariant().getPrice());
            orderDetailRepository.save(detail);
        }

        cartService.clearCart(username);
        
        // Auto-ẩn sản phẩm hết hàng
        checkAndHideOutOfStockProducts(savedOrder.getId());
        
        return savedOrder;
    }

    @Override
    public List<Order> getOrdersByUsername(String username) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("KhÃ´ng tÃ¬m tháº¥y ngÆ°á»i dÃ¹ng"));
        return orderRepository.findByUserId(user.getId());
    }

    @Override
    public Order getOrderById(Long orderId) {
        return orderRepository.findById(orderId)
                .orElseThrow(() -> new RuntimeException("KhÃ´ng tÃ¬m tháº¥y Ä‘Æ¡n hÃ ng"));
    }

    @Override
    @Transactional
    public void updateOrderStatus(Long orderId, String status) {
        Order order = getOrderById(orderId);
        
        // Cáº¥p Ä‘iá»ƒm thÆ°á»Ÿng náº¿u tráº¡ng thÃ¡i chuyá»ƒn sang DELIVERED vÃ  tráº¡ng thÃ¡i cÅ© chÆ°a pháº£i DELIVERED
        if ("DELIVERED".equals(status) && !"DELIVERED".equals(order.getStatus())) {
            Double amount = order.getFinalAmount();
            if (amount != null && amount > 0) {
                int points = (int) (amount / 10000.0);
                if (points > 0) {
                    User user = order.getUser();
                    UserProfile profile = userProfileRepository.findById(user.getId()).orElse(null);
                    if (profile != null) {
                        profile.setLoyaltyPoints((profile.getLoyaltyPoints() == null ? 0 : profile.getLoyaltyPoints()) + points);
                        userProfileRepository.save(profile);
                        
                        PointTransaction trans = new PointTransaction();
                        trans.setUser(user);
                        trans.setPoints(points);
                        
                        String payment = order.getPayments();
                        if (payment != null && payment.toUpperCase().contains("COD")) {
                            trans.setTransactionType("Thanh toÃ¡n khi nháº­n hÃ ng (COD)");
                        } else if (payment != null && payment.toUpperCase().contains("CK")) {
                            trans.setTransactionType("Thanh toÃ¡n chuyá»ƒn khoáº£n");
                        } else {
                            trans.setTransactionType("Thanh toÃ¡n Ä‘Æ¡n hÃ ng (" + (payment != null ? payment : "KhÃ¡c") + ")");
                        }
                        
                        pointTransactionRepository.save(trans);
                    }
                }
            }
        }
        
        order.setStatus(status);
        orderRepository.save(order);
    }

    @Override
    public List<Order> getAllOrders() {
        return orderRepository.findAll();
    }

    @Override
    @Transactional
    public void deleteOrder(Long orderId) {
        Order order = getOrderById(orderId);
        orderDetailRepository.deleteAll(orderDetailRepository.findByOrderId(orderId));
        orderRepository.delete(order);
    }

    @Override
    @Transactional
    public void cancelOrder(Long orderId, String username) {
        Order order = getOrderById(orderId);

        // Kiểm tra quyền
        if (!order.getUser().getUsername().equals(username)) {
            throw new RuntimeException("Không có quyền hủy đơn hàng này");
        }

        // Chỉ cho phép hủy đơn đang PENDING
        if (!"PENDING".equals(order.getStatus())) {
            throw new RuntimeException("Chỉ có thể hủy đơn hàng đang chờ xử lý");
        }

        // Hoàn lại số lượng tồn kho
        List<OrderDetail> details = orderDetailRepository.findByOrderId(orderId);
        for (OrderDetail detail : details) {
            ProductVariant variant = detail.getProductVariant();
            variant.setStock(variant.getStock() + detail.getQuantity());
            variantRepository.save(variant);

            // Nếu sản phẩm bị ẩn do hết hàng, bật lại
            if (!variant.getProduct().getIsActive()) {
                variant.getProduct().setIsActive(true);
            }
        }

        // Hoàn lại điểm tích lũy nếu đã dùng
        if (order.getDiscountAmount() != null && order.getDiscountAmount() > 0) {
            int pointsToRefund = (int) ((order.getDiscountAmount() / 10000.0) * 100);
            UserProfile profile = userProfileRepository.findById(order.getUser().getId()).orElse(null);
            if (profile != null) {
                profile.setLoyaltyPoints((profile.getLoyaltyPoints() == null ? 0 : profile.getLoyaltyPoints()) + pointsToRefund);
                userProfileRepository.save(profile);

                PointTransaction pt = new PointTransaction();
                pt.setUser(order.getUser());
                pt.setPoints(pointsToRefund);
                pt.setTransactionType("REFUND_CANCEL_ORDER");
                pointTransactionRepository.save(pt);
            }
        }

        order.setStatus("CANCELLED");
        orderRepository.save(order);
    }

    /**
     * Kiểm tra và ẩn sản phẩm nếu TẤT CẢ variant đều hết hàng (stock=0).
     */
    private void checkAndHideOutOfStockProducts(Long orderId) {
        List<OrderDetail> details = orderDetailRepository.findByOrderId(orderId);
        for (OrderDetail detail : details) {
            ProductVariant variant = detail.getProductVariant();
            if (variant.getStock() != null && variant.getStock() <= 0) {
                var product = variant.getProduct();
                List<ProductVariant> allVariants = variantRepository.findByProductId(product.getId());
                boolean allOutOfStock = allVariants.stream().allMatch(v -> v.getStock() == null || v.getStock() <= 0);
                if (allOutOfStock) {
                    product.setIsActive(false);
                }
            }
        }
    }
}