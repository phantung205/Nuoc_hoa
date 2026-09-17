package com.perfumes.nuochoa.service;

import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class OtpService {

    private static class OtpData {
        String code;
        LocalDateTime expiryTime;

        OtpData(String code, LocalDateTime expiryTime) {
            this.code = code;
            this.expiryTime = expiryTime;
        }
    }

    // Lưu mã OTP theo Email trong bộ nhớ RAM
    private final Map<String, OtpData> otpStorage = new ConcurrentHashMap<>();

    // Sinh mã OTP 6 số ngẫu nhiên và đặt thời gian hết hạn sau 2 phút
    public String generateOtp(String email) {
        String otp = String.format("%06d", new Random().nextInt(900000) + 100000);
        LocalDateTime expiryTime = LocalDateTime.now().plusMinutes(2);

        otpStorage.put(email, new OtpData(otp, expiryTime));
        return otp;
    }

    // Kiểm tra tính hợp lệ của mã OTP
    public boolean validateOtp(String email, String inputOtp) {
        OtpData data = otpStorage.get(email);

        if (data == null) {
            return false;
        }

        // Hết hạn 2 phút
        if (LocalDateTime.now().isAfter(data.expiryTime)) {
            otpStorage.remove(email);
            return false;
        }

        // Khớp mã OTP
        if (data.code.equals(inputOtp)) {
            otpStorage.remove(email);
            return true;
        }

        return false;
    }

    // Xóa OTP khỏi bộ nhớ khi không cần dùng nữa
    public void clearOtp(String email) {
        otpStorage.remove(email);
    }
}