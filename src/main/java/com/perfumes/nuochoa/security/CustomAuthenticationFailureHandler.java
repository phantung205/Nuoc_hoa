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
    public void onAuthenticationFailure(HttpServletRequest request, HttpServletResponse response, AuthenticationException exception) throws IOException, ServletException {
        // Đúng mật khẩu nhưng tài khoản đang ở trạng thái UNVERIFIED
        if (exception instanceof DisabledException) {
            String username = request.getParameter("username");
            User user = userRepository.findByUsername(username).orElse(null);

            if (user != null && "UNVERIFIED".equalsIgnoreCase(user.getStatus())) {
                String otp = otpService.generateOtp(user.getEmail());
                emailService.sendOtpEmail(user.getEmail(), otp);

                request.getSession().setAttribute("pendingEmail", user.getEmail());
                getRedirectStrategy().sendRedirect(request, response, "/auth/verify-otp?unverified=true");
                return;
            }
        }

        // Nhập sai tên đăng nhập hoặc sai mật khẩu thông thường
        getRedirectStrategy().sendRedirect(request, response, "/auth/login?error=true");
    }
}