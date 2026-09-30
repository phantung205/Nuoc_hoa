package com.perfumes.nuochoa.controller.web;

import com.perfumes.nuochoa.service.VoucherService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/vouchers")
public class VoucherApiController {

    private final VoucherService voucherService;

    public VoucherApiController(VoucherService voucherService) {
        this.voucherService = voucherService;
    }

    @PostMapping("/apply")
    public ResponseEntity<?> applyVoucher(@RequestParam("code") String code, 
                                          @RequestParam("orderTotal") Double orderTotal) {
        Map<String, Object> response = new HashMap<>();
        try {
            Double discount = voucherService.calculateDiscount(code, orderTotal);
            response.put("success", true);
            response.put("discountAmount", discount);
            response.put("message", "Áp dụng mã giảm giá thành công!");
            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            response.put("success", false);
            response.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }
}
