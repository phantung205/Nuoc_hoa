package com.perfumes.nuochoa.controller.auth;

import com.perfumes.nuochoa.dto.RegisterRequest;
import com.perfumes.nuochoa.entity.User;
import com.perfumes.nuochoa.service.EmailService;
import com.perfumes.nuochoa.service.OtpService;
import com.perfumes.nuochoa.service.UserService;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

@Controller
@RequestMapping("/auth")
public class AuthController {

    private final UserService userService;
    private final OtpService otpService;
    private final EmailService emailService;

    public AuthController(UserService userService, OtpService otpService, EmailService emailService) {
        this.userService = userService;
        this.otpService = otpService;
        this.emailService = emailService;
    }


    @GetMapping("/register")
    public String showRegisterForm(Model model) {
        model.addAttribute("registerRequest", new RegisterRequest());
        return "auth/register";
    }

    @PostMapping("/register")
    public String processRegister(@Valid @ModelAttribute("registerRequest") RegisterRequest registerRequest,
                                  BindingResult bindingResult,
                                  Model model,
                                  HttpSession session) {
        // @Valid kiểm tra các ràng buộc trong RegisterRequest (@NotBlank, @Email...)
        if (bindingResult.hasErrors()) {
            return "auth/register";
        }
        try {
            userService.registerUser(registerRequest);

            // Lưu email vào session để trang verify-otp biết cần xác thực cho ai
            session.setAttribute("pendingEmail", registerRequest.getEmail());

            // Tạo OTP và gửi email xác thực
            String otp = otpService.generateOtp(registerRequest.getEmail());
            emailService.sendOtpEmail(registerRequest.getEmail(), otp);

            return "redirect:/auth/verify-otp";

        } catch (Exception ex) {
            model.addAttribute("errorMessage", ex.getMessage());
            return "auth/register";
        }
    }

    @GetMapping("/verify-otp")
    public String showVerifyOtpForm(@RequestParam(value = "unverified", required = false) String unverified,
                                    HttpSession session,
                                    Model model) {
        String email = (String) session.getAttribute("pendingEmail");
        if (email == null) {
            return "redirect:/auth/register";
        }

        // Tham số "unverified" xuất hiện khi user cố đăng nhập với tài khoản chưa xác thực
        if (unverified != null) {
            model.addAttribute("errorMessage", "Tài khoản chưa được xác thực! Mã OTP mới đã được gửi tới email.");
        }

        model.addAttribute("email", email);
        model.addAttribute("isResetPassword", false); // Template dùng flag này để phân biệt 2 luồng
        return "auth/verify-otp";
    }

    @PostMapping("/verify-otp")
    public String processVerifyOtp(@RequestParam("otp") String otp,
                                   HttpSession session,
                                   Model model) {
        String email = (String) session.getAttribute("pendingEmail");
        if (email == null) {
            return "redirect:/auth/register";
        }

        if (otpService.validateOtp(email, otp)) {
            userService.enableUser(email); // Chuyển status UNVERIFIED → ACTIVE
            session.removeAttribute("pendingEmail");
            return "redirect:/auth/login?registered=true";
        } else {
            model.addAttribute("errorMessage", "Mã OTP không chính xác hoặc đã hết hạn (2 phút)!");
            model.addAttribute("email", email);
            model.addAttribute("isResetPassword", false);
            return "auth/verify-otp";
        }
    }

    @PostMapping("/resend-otp")
    public String resendOtp(HttpSession session, Model model) {
        String email = (String) session.getAttribute("pendingEmail");
        if (email == null) {
            return "redirect:/auth/register";
        }

        String newOtp = otpService.generateOtp(email);
        emailService.sendOtpEmail(email, newOtp);

        model.addAttribute("successMessage", "Đã gửi lại mã OTP mới về email của bạn!");
        model.addAttribute("email", email);
        model.addAttribute("isResetPassword", false);
        return "auth/verify-otp";
    }


    @GetMapping("/forgot-password")
    public String showForgotPasswordForm() {
        return "auth/forgot-password";
    }

    @PostMapping("/forgot-password")
    public String processForgotPassword(@RequestParam("identifier") String identifier,
                                        HttpSession session,
                                        Model model) {
        try {
            User user = userService.findByUsernameOrEmail(identifier);

            // Gửi OTP tới email của tài khoản tìm được
            String otp = otpService.generateOtp(user.getEmail());
            emailService.sendOtpEmail(user.getEmail(), otp);

            session.setAttribute("resetEmail", user.getEmail());
            return "redirect:/auth/verify-reset-otp";

        } catch (Exception e) {
            model.addAttribute("errorMessage", e.getMessage());
            return "auth/forgot-password";
        }
    }


    @GetMapping("/verify-reset-otp")
    public String showVerifyResetOtpForm(HttpSession session, Model model) {
        String email = (String) session.getAttribute("resetEmail");
        if (email == null) {
            return "redirect:/auth/forgot-password";
        }

        model.addAttribute("email", email);
        model.addAttribute("isResetPassword", true); // Flag để template hiển thị đúng nội dung
        return "auth/verify-otp";
    }


    @PostMapping("/verify-reset-otp")
    public String processVerifyResetOtp(@RequestParam("otp") String otp,
                                        HttpSession session,
                                        Model model) {
        String email = (String) session.getAttribute("resetEmail");
        if (email == null) {
            return "redirect:/auth/forgot-password";
        }

        if (otpService.validateOtp(email, otp)) {
            // Đặt cờ "được phép đặt lại mật khẩu" vào session
            session.setAttribute("canResetPassword", true);
            return "redirect:/auth/reset-password";
        } else {
            model.addAttribute("errorMessage", "Mã OTP không chính xác hoặc đã hết hạn (2 phút)!");
            model.addAttribute("email", email);
            model.addAttribute("isResetPassword", true);
            return "auth/verify-otp";
        }
    }


    @PostMapping("/resend-reset-otp")
    public String resendResetOtp(HttpSession session, Model model) {
        String email = (String) session.getAttribute("resetEmail");
        if (email == null) {
            return "redirect:/auth/forgot-password";
        }

        String newOtp = otpService.generateOtp(email);
        emailService.sendOtpEmail(email, newOtp);
        model.addAttribute("successMessage", "Đã gửi lại mã OTP mới về email của bạn!");
        model.addAttribute("email", email);
        model.addAttribute("isResetPassword", true);
        return "auth/verify-otp";
    }


    @GetMapping("/reset-password")
    public String showResetPasswordForm(HttpSession session) {
        Boolean canReset = (Boolean) session.getAttribute("canResetPassword");

        // Chặn truy cập trực tiếp URL mà không qua bước xác thực OTP
        if (canReset == null || !canReset) {
            return "redirect:/auth/forgot-password";
        }
        return "auth/reset-password";
    }

    @PostMapping("/reset-password")
    public String processResetPassword(@RequestParam("password") String password,
                                       @RequestParam("confirmPassword") String confirmPassword,
                                       HttpSession session,
                                       Model model) {
        String email = (String) session.getAttribute("resetEmail");
        if (email == null) {
            return "redirect:/auth/forgot-password";
        }

        if (!password.equals(confirmPassword)) {
            model.addAttribute("errorMessage", "Mật khẩu xác nhận không khớp!");
            return "auth/reset-password";
        }

        userService.updatePassword(email, password);

        // Dọn dẹp session sau khi hoàn tất luồng quên mật khẩu
        session.removeAttribute("resetEmail");
        session.removeAttribute("canResetPassword");

        return "redirect:/auth/login?resetSuccess=true";
    }

    @GetMapping("/login")
    public String showLoginForm(@RequestParam(value = "registered", required = false) String registered,
                                @RequestParam(value = "resetSuccess", required = false) String resetSuccess,
                                @RequestParam(value = "error", required = false) String error,
                                @RequestParam(value = "logout", required = false) String logout,
                                Model model) {
        if (registered != null) {
            model.addAttribute("successMessage", "Xác thực tài khoản thành công! Vui lòng đăng nhập.");
        }
        if (resetSuccess != null) {
            model.addAttribute("successMessage", "Đặt lại mật khẩu thành công! Vui lòng đăng nhập bằng mật khẩu mới.");
        }
        if (error != null) {
            model.addAttribute("errorMessage", "Tên đăng nhập hoặc mật khẩu không chính xác!");
        }
        if (logout != null) {
            model.addAttribute("successMessage", "Đã đăng xuất thành công!");
        }
        return "auth/login";
    }
}