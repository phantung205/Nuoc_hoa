package com.perfumes.nuochoa.service;

import org.springframework.stereotype.Service;

import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;


@Service
public class OtpService {

    private static final int OTP_LENGTH = 6;

    private static final int OTP_EXPIRY_MINUTES = 2;

    private final SecureRandom secureRandom = new SecureRandom();


    private final Map<String, OtpData> otpStorage = new ConcurrentHashMap<>();


    public String generateOtp(String email) {
        String otpCode = generateRandomCode();
        LocalDateTime expiryTime = LocalDateTime.now().plusMinutes(OTP_EXPIRY_MINUTES);

        otpStorage.put(email, new OtpData(otpCode, expiryTime));
        return otpCode;
    }


    public boolean validateOtp(String email, String inputOtp) {
        OtpData storedOtp = otpStorage.get(email);

        // Không tồn tại OTP cho email này
        if (storedOtp == null) {
            return false;
        }

        // OTP đã hết hạn → xóa và từ chối
        if (LocalDateTime.now().isAfter(storedOtp.expiryTime)) {
            otpStorage.remove(email);
            return false;
        }

        // OTP khớp → xóa để không dùng lại được, trả về thành công
        if (storedOtp.code.equals(inputOtp)) {
            otpStorage.remove(email);
            return true;
        }

        // OTP sai
        return false;
    }

    /** Xóa OTP của email khỏi bộ nhớ (dùng khi cần hủy OTP thủ công). */
    public void clearOtp(String email) {
        otpStorage.remove(email);
    }


    private String generateRandomCode() {
        int maxValue = (int) Math.pow(10, OTP_LENGTH); // 10^6 = 1_000_000
        int code = secureRandom.nextInt(maxValue);
        // %06d đảm bảo luôn có đủ 6 chữ số, thêm số 0 ở đầu nếu cần (VD: 47 → "000047")
        return String.format("%0" + OTP_LENGTH + "d", code);
    }


    private static class OtpData {
        final String code;
        final LocalDateTime expiryTime;

        OtpData(String code, LocalDateTime expiryTime) {
            this.code = code;
            this.expiryTime = expiryTime;
        }
    }
}