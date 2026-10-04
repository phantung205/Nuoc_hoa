package com.perfumes.nuochoa.service;

import com.perfumes.nuochoa.entity.Voucher;

public interface VoucherService {

    Voucher findByCode(String code);
    

    Double calculateDiscount(String code, Double orderTotal);

    void useVoucher(String code);

    java.util.List<Voucher> getAllVouchers();
    Voucher getVoucherById(Long id);
    Voucher createVoucher(Voucher voucher);
    Voucher updateVoucher(Long id, Voucher voucher);
    void toggleStatus(Long id);
    void deleteVoucher(Long id);

    void claimVoucher(Long voucherId, String username);
    java.util.List<com.perfumes.nuochoa.entity.UserVoucher> getUserVouchers(Long userId);

}



