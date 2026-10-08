package com.perfumes.nuochoa.service.impl;

import com.perfumes.nuochoa.entity.*;
import com.perfumes.nuochoa.repository.*;
import com.perfumes.nuochoa.service.CartService;
import com.perfumes.nuochoa.service.OrderService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
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
    private final VoucherRepository voucherRepository;
    private final UserVoucherRepository userVoucherRepository;

    public OrderServiceImpl(OrderRepository orderRepository,
                            OrderDetailRepository orderDetailRepository,
                            UserRepository userRepository,
                            CartService cartService,
                            ProductVariantRepository variantRepository,
                            UserProfileRepository userProfileRepository,
                            PointTransactionRepository pointTransactionRepository,
                            VoucherRepository voucherRepository,
                            UserVoucherRepository userVoucherRepository) {
        this.orderRepository = orderRepository;
        this.orderDetailRepository = orderDetailRepository;
        this.userRepository = userRepository;
        this.cartService = cartService;
        this.variantRepository = variantRepository;
        this.userProfileRepository = userProfileRepository;
        this.pointTransactionRepository = pointTransactionRepository;
        this.voucherRepository = voucherRepository;
        this.userVoucherRepository = userVoucherRepository;
    }

    @Override
    @Transactional
    public Order createOrder(String username, UserAddress shippingAddress, String paymentMethod, String note, Integer pointsToUse, Long voucherId) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy người dùng"));

        List<CartItem> cartItems = cartService.getCartItemsByUsername(username);
        if (cartItems.isEmpty()) {
            throw new RuntimeException("Giỏ hàng đang trống");
        }

        Double totalAmount = 0.0;
        for (CartItem item : cartItems) {
            ProductVariant variant = item.getProductVariant();
            if (variant.getStock() < item.getQuantity()) {
                throw new RuntimeException("Sản phẩm " + variant.getProduct().getName() + " không đủ số lượng trong kho");
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

        Voucher voucher = null;
        if (voucherId != null) {
            voucher = voucherRepository.findById(voucherId).orElse(null);
            if (voucher != null) {
                if (voucher.getIsActive() != null && !voucher.getIsActive()) {
                    throw new RuntimeException("Voucher không hoạt động");
                }
                LocalDate now = LocalDate.now();
                if (voucher.getStartDate() != null && now.isBefore(voucher.getStartDate())) {
                    throw new RuntimeException("Voucher chưa đến ngày sử dụng");
                }
                if (voucher.getEndDate() != null && now.isAfter(voucher.getEndDate())) {
                    throw new RuntimeException("Voucher đã hết hạn");
                }
                if (voucher.getUsageLimit() != null && voucher.getUsageCount() != null && voucher.getUsageCount() >= voucher.getUsageLimit()) {
                    throw new RuntimeException("Voucher đã hết lượt sử dụng");
                }
                if (voucher.getMinOrderValue() != null && totalAmount < voucher.getMinOrderValue()) {
                    throw new RuntimeException("Đơn hàng chưa đạt giá trị tối thiểu để sử dụng voucher này");
                }

                UserVoucher uv = userVoucherRepository.findByUserIdAndVoucherId(user.getId(), voucherId).orElse(null);
                if (uv != null && uv.getIsUsed() != null && uv.getIsUsed()) {
                    throw new RuntimeException("Bạn đã sử dụng voucher này rồi");
                }

                Double vDiscount = 0.0;
                if ("PERCENT".equalsIgnoreCase(voucher.getDiscountType())) {
                    vDiscount = totalAmount * (voucher.getDiscountValue() / 100.0);
                    if (voucher.getMaxDiscountAmount() != null && vDiscount > voucher.getMaxDiscountAmount()) {
                        vDiscount = voucher.getMaxDiscountAmount();
                    }
                } else if ("FIXED".equalsIgnoreCase(voucher.getDiscountType())) {
                    vDiscount = voucher.getDiscountValue();
                }

                discountAmount += vDiscount;
                if (discountAmount > totalAmount) {
                    discountAmount = totalAmount;
                }

                voucher.setUsageCount((voucher.getUsageCount() == null ? 0 : voucher.getUsageCount()) + 1);
                voucherRepository.save(voucher);

                if (uv != null) {
                    uv.setIsUsed(true);
                    userVoucherRepository.save(uv);
                }
            }
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
        if (voucher != null) {
            order.setVoucher(voucher);
        }

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
                .orElseThrow(() -> new RuntimeException("Không tìm thấy người dùng"));
        return orderRepository.findByUserId(user.getId());
    }

    @Override
    public Order getOrderById(Long orderId) {
        return orderRepository.findById(orderId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy đơn hàng"));
    }

    @Override
    @Transactional
    public void updateOrderStatus(Long orderId, String status) {
        Order order = getOrderById(orderId);

        // Cấp điểm thưởng nếu trạng thái chuyển sang DELIVERED và trạng thái cũ chưa phải DELIVERED
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
                            trans.setTransactionType("Thanh toán khi nhận hàng (COD)");
                        } else if (payment != null && payment.toUpperCase().contains("CK")) {
                            trans.setTransactionType("Thanh toán chuyển khoản");
                        } else {
                            trans.setTransactionType("Thanh toán đơn hàng (" + (payment != null ? payment : "Khác") + ")");
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

        processOrderCancellation(order);

        order.setStatus("CANCELLED");
        orderRepository.save(order);
    }

    @Override
    @Transactional
    public void revertUnpaidOrder(Long orderId, String username) {
        Order order = getOrderById(orderId);

        // Kiểm tra quyền
        if (!order.getUser().getUsername().equals(username)) {
            throw new RuntimeException("Không có quyền hủy đơn hàng này");
        }

        processOrderCancellation(order);

        order.setStatus("CANCELLED");
        orderRepository.save(order);
    }

    private void processOrderCancellation(Order order) {
        // Hoàn lại số lượng tồn kho
        List<OrderDetail> details = orderDetailRepository.findByOrderId(order.getId());
        for (OrderDetail detail : details) {
            ProductVariant variant = detail.getProductVariant();
            variant.setStock(variant.getStock() + detail.getQuantity());
            variantRepository.save(variant);

            // Nếu sản phẩm bị ẩn do hết hàng, bật lại
            if (!variant.getProduct().getIsActive()) {
                variant.getProduct().setIsActive(true);
            }
        }

        // Hoàn lại voucher
        if (order.getVoucher() != null) {
            Voucher voucher = order.getVoucher();
            voucher.setUsageCount(Math.max(0, (voucher.getUsageCount() == null ? 0 : voucher.getUsageCount()) - 1));
            voucherRepository.save(voucher);

            UserVoucher uv = userVoucherRepository.findByUserIdAndVoucherId(order.getUser().getId(), voucher.getId()).orElse(null);
            if (uv != null) {
                uv.setIsUsed(false);
                userVoucherRepository.save(uv);
            }
        }

        // Hoàn lại điểm tích lũy nếu đã dùng
        if (order.getDiscountAmount() != null && order.getDiscountAmount() > 0) {
            Double voucherDiscount = 0.0;
            if (order.getVoucher() != null) {
                Voucher v = order.getVoucher();
                if ("PERCENT".equalsIgnoreCase(v.getDiscountType())) {
                    voucherDiscount = order.getTotalAmount() * (v.getDiscountValue() / 100.0);
                    if (v.getMaxDiscountAmount() != null && voucherDiscount > v.getMaxDiscountAmount()) {
                        voucherDiscount = v.getMaxDiscountAmount();
                    }
                } else if ("FIXED".equalsIgnoreCase(v.getDiscountType())) {
                    voucherDiscount = v.getDiscountValue();
                }
            }

            Double pointsDiscount = order.getDiscountAmount() - voucherDiscount;

            // Xử lý làm tròn để tránh sai số dấu phẩy động
            if (pointsDiscount > 1.0) {
                int pointsToRefund = (int) Math.round((pointsDiscount / 10000.0) * 100);
                if (pointsToRefund > 0) {
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
            }
        }
    }


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