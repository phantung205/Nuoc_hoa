package com.perfumes.nuochoa.service;

import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;


@Service
public class EmailService {

    private static final String EMAIL_SUBJECT = "[Nước Hoa Shop] Mã xác thực OTP của bạn";

    private final JavaMailSender mailSender;

    public EmailService(JavaMailSender mailSender) {
        this.mailSender = mailSender;
    }


    public void sendOtpEmail(String toEmail, String otpCode) {
        String emailBody = "Mã OTP của bạn là: " + otpCode + "\n"
                + "Mã này có hiệu lực trong 2 phút.\n"
                + "Vui lòng không chia sẻ mã này cho bất kỳ ai.";

        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(toEmail);
        message.setSubject(EMAIL_SUBJECT);
        message.setText(emailBody);

        mailSender.send(message);
    }
}