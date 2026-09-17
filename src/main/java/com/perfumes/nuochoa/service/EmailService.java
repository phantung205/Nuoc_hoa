package com.perfumes.nuochoa.service;

import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
public class EmailService {

    private final JavaMailSender mailSender;

    public EmailService(JavaMailSender mailSender) {
        this.mailSender = mailSender;
    }

    public void sendOtpEmail(String toEmail, String otpCode) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(toEmail);
        message.setSubject("[Nước Hoa Shop] Mã xác thực OTP đăng ký tài khoản");
        message.setText("Mã OTP của bạn là: " + otpCode + "\nMã này có hiệu lực trong 2 phút. Vui lòng không chia sẻ mã này cho ai.");

        mailSender.send(message);
    }
}