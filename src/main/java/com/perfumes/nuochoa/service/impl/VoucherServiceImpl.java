package com.perfumes.nuochoa.service.impl;

import com.perfumes.nuochoa.entity.Voucher;
import com.perfumes.nuochoa.repository.VoucherRepository;
import com.perfumes.nuochoa.service.VoucherService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import com.perfumes.nuochoa.entity.User;
import com.perfumes.nuochoa.entity.UserVoucher;
import com.perfumes.nuochoa.entity.Order;
import com.perfumes.nuochoa.repository.UserRepository;
import com.perfumes.nuochoa.repository.UserVoucherRepository;
import com.perfumes.nuochoa.repository.OrderRepository;
import com.perfumes.nuochoa.repository.UserProfileRepository;
import com.perfumes.nuochoa.entity.UserProfile;
import java.util.List;

@Service
public class VoucherServiceImpl implements VoucherService {

    private final VoucherRepository voucherRepository;
    private final UserRepository userRepository;
    private final UserVoucherRepository userVoucherRepository;
    private final OrderRepository orderRepository;
    private final UserProfileRepository userProfileRepository;

    public UserRepository getUserRepository() { return userRepository; }
    public UserVoucherRepository getUserVoucherRepository() { return userVoucherRepository; }

    public VoucherServiceImpl(VoucherRepository voucherRepository, UserRepository userRepository, UserVoucherRepository userVoucherRepository, OrderRepository orderRepository, UserProfileRepository userProfileRepository) {
        this.voucherRepository = voucherRepository;
        this.userRepository = userRepository;
        this.userVoucherRepository = userVoucherRepository;
        this.orderRepository = orderRepository;
        this.userProfileRepository = userProfileRepository;
    }

    @Override
    public Voucher findByCode(String code) {
        return voucherRepository.findByCode(code).orElse(null);
    }

    @Override
    public Double calculateDiscount(String code, Double orderTotal) {
        Voucher voucher = findByCode(code);
        if (voucher == null) {
            throw new RuntimeException("Mã giảm giá không tồn tại!");
        }

        if (voucher.getIsActive() != null && !voucher.getIsActive()) {
            throw new RuntimeException("Mã giảm giá đã bị khóa hoặc không còn hoạt động!");
        }

        LocalDate today = LocalDate.now();
        if (voucher.getStartDate() != null && today.isBefore(voucher.getStartDate())) {
            throw new RuntimeException("Mã giảm giá chưa đến thời gian sử dụng!");
        }
        if (voucher.getEndDate() != null && today.isAfter(voucher.getEndDate())) {
            throw new RuntimeException("Mã giảm giá đã hết hạn!");
        }

        if (voucher.getUsageLimit() != null && voucher.getUsageCount() >= voucher.getUsageLimit()) {
            throw new RuntimeException("Mã giảm giá đã hết lượt sử dụng!");
        }

        if (voucher.getMinOrderValue() != null && orderTotal < voucher.getMinOrderValue()) {
            throw new RuntimeException("Đơn hàng chưa đạt giá trị tối thiểu " + voucher.getMinOrderValue() + " để dùng mã này!");
        }

        // Tính toán giảm giá
        double discountAmount = 0.0;
        if ("PERCENT".equalsIgnoreCase(voucher.getDiscountType())) {
            discountAmount = orderTotal * (voucher.getDiscountValue() / 100.0);
            if (voucher.getMaxDiscountAmount() != null && discountAmount > voucher.getMaxDiscountAmount()) {
                discountAmount = voucher.getMaxDiscountAmount();
            }
        } else if ("FIXED".equalsIgnoreCase(voucher.getDiscountType())) {
            discountAmount = voucher.getDiscountValue();
        }

        // Không cho phép giảm quá tổng tiền
        if (discountAmount > orderTotal) {
            discountAmount = orderTotal;
        }

        return discountAmount;
    }

    @Override
    @Transactional
    public void useVoucher(String code) {
        Voucher voucher = findByCode(code);
        if (voucher != null) {
            voucher.setUsageCount(voucher.getUsageCount() + 1);
            voucherRepository.save(voucher);
        }
    }


    @Override
    public java.util.List<Voucher> getAllVouchers() {
        return voucherRepository.findAll();
    }

    @Override
    public Voucher getVoucherById(Long id) {
        return voucherRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy voucher với ID: " + id));
    }

    @Override
    @Transactional
    public Voucher createVoucher(Voucher voucher) {
        if (voucherRepository.findByCode(voucher.getCode()).isPresent()) {
            throw new RuntimeException("Mã voucher đã tồn tại: " + voucher.getCode());
        }
        if (voucher.getUsageCount() == null) {
            voucher.setUsageCount(0);
        }
        if (voucher.getIsActive() == null) {
            voucher.setIsActive(true);
        }
        return voucherRepository.save(voucher);
    }

    @Override
    @Transactional
    public Voucher updateVoucher(Long id, Voucher voucher) {
        Voucher existing = getVoucherById(id);

        // Kiểm tra trùng mã code nếu có thay đổi code
        if (!existing.getCode().equals(voucher.getCode()) &&
                voucherRepository.findByCode(voucher.getCode()).isPresent()) {
            throw new RuntimeException("Mã voucher đã tồn tại: " + voucher.getCode());
        }

        existing.setCode(voucher.getCode());
        existing.setDiscountType(voucher.getDiscountType());
        existing.setDiscountValue(voucher.getDiscountValue());
        existing.setMinOrderValue(voucher.getMinOrderValue());
        existing.setMaxDiscountAmount(voucher.getMaxDiscountAmount());
        existing.setUsageLimit(voucher.getUsageLimit());
        existing.setStartDate(voucher.getStartDate());
        existing.setEndDate(voucher.getEndDate());
        existing.setIsActive(voucher.getIsActive());

        // Các trường nhiệm vụ nhận Voucher
        existing.setReceiveMethod(voucher.getReceiveMethod());
        existing.setReceiveDescription(voucher.getReceiveDescription());

        return voucherRepository.save(existing);
    }

    @Override
    @Transactional
    public void toggleStatus(Long id) {
        Voucher voucher = getVoucherById(id);
        voucher.setIsActive(!voucher.getIsActive());
        voucherRepository.save(voucher);
    }

    @Override
    public void deleteVoucher(Long id) {
        if (!voucherRepository.existsById(id)) {
            throw new RuntimeException("Không tìm thấy voucher với ID: " + id);
        }

        // Xóa tham chiếu voucher trong các đơn hàng để tránh lỗi khóa ngoại
        List<Order> orders = orderRepository.findByVoucherId(id);
        for (Order order : orders) {
            order.setVoucher(null);
            orderRepository.save(order);
        }

        // Xóa các bản ghi UserVoucher liên quan để tài khoản bị mất voucher
        userVoucherRepository.deleteByVoucherId(id);

        voucherRepository.deleteById(id);
    }

    @Override
    @Transactional
    public java.util.List<com.perfumes.nuochoa.entity.UserVoucher> getUserVouchers(Long userId) {
        return userVoucherRepository.findByUserId(userId);
    }

    @Override
    @Transactional
    public void claimVoucher(Long voucherId, String username) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy user"));

        Voucher voucher = voucherRepository.findById(voucherId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy voucher"));

        if (!voucher.getIsActive() || (voucher.getEndDate() != null && voucher.getEndDate().isBefore(LocalDate.now()))) {
            throw new RuntimeException("Voucher không còn hoạt động hoặc đã hết hạn");
        }

        if (voucher.getUsageLimit() != null && voucher.getUsageCount() >= voucher.getUsageLimit()) {
            throw new RuntimeException("Voucher đã hết lượt sử dụng");
        }

        if (userVoucherRepository.existsByUserIdAndVoucherId(user.getId(), voucher.getId())) {
            throw new RuntimeException("Bạn đã nhận voucher này rồi");
        }

        String method = voucher.getReceiveMethod();
        Double req = 0.0;

        if ("ORDER_VALUE".equals(method)) {
            List<Order> orders = orderRepository.findByUserId(user.getId());
            double totalSpent = orders.stream()
                    .filter(o -> "COMPLETED".equals(o.getStatus()))
                    .mapToDouble(Order::getTotalAmount)
                    .sum();
            if (totalSpent < req) {
                throw new RuntimeException("Tổng giá trị đơn hàng hoàn thành của bạn chưa đạt mức yêu cầu (" + req + " VND)");
            }
        }

        UserVoucher uv = new UserVoucher();
        uv.setUser(user);
        uv.setVoucher(voucher);
        uv.setIsUsed(false);
        userVoucherRepository.save(uv);
    }
}
