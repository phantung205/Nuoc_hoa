package com.perfumes.nuochoa.security;

import com.perfumes.nuochoa.entity.User;
import com.perfumes.nuochoa.repository.UserRepository;
import com.perfumes.nuochoa.service.EmailService;
import com.perfumes.nuochoa.service.OtpService;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationFailureHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;


@Component
public class CustomAuthenticationFailureHandler extends SimpleUrlAuthenticationFailureHandler {

    private final UserRepository userRepository;
    private final OtpService otpService;
    private final EmailService emailService;

    public CustomAuthenticationFailureHandler(UserRepository userRepository,
                                              OtpService otpService,
                                              EmailService emailService) {
        this.userRepository = userRepository;
        this.otpService = otpService;
        this.emailService = emailService;
    }

    @Override
    public void onAuthenticationFailure(HttpServletRequest request,
                                        HttpServletResponse response,
                                        AuthenticationException exception) throws IOException, ServletException {

        // chưa kích hoạt
        if (exception instanceof DisabledException) {
            handleUnverifiedAccount(request, response);
            return;
        }

        // sai tài khoản mật khẩu
        getRedirectStrategy().sendRedirect(request, response, "/auth/login?error=true");
    }


    private void handleUnverifiedAccount(HttpServletRequest request,
                                         HttpServletResponse response) throws IOException {
        String username = request.getParameter("username");
        User user = userRepository.findByUsername(username).orElse(null);

        // nếu tìm thấy và trạng thái đang ko hoạt động
        if (user != null && "UNVERIFIED".equalsIgnoreCase(user.getStatus())) {
            // tạo otp
            String newOtp = otpService.generateOtp(user.getEmail());
            emailService.sendOtpEmail(user.getEmail(), newOtp);

            // Lưu email vào session để trang verify-otp biết cần xác thực cho ai
            request.getSession().setAttribute("pendingEmail", user.getEmail());
            // chuyển hướng người dùng sang trang otp
            getRedirectStrategy().sendRedirect(request, response, "/auth/verify-otp?unverified=true");
        } else {
            // Trường hợp bất thường: không tìm thấy user nhưng vẫn bị DisabledException
            getRedirectStrategy().sendRedirect(request, response, "/auth/login?error=true");
        }
    }
}