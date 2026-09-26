package com.perfumes.nuochoa.config;

import com.perfumes.nuochoa.security.CustomAuthenticationFailureHandler;
import com.perfumes.nuochoa.security.CustomUserDetailsService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;



@Configuration
@EnableWebSecurity
public class SecurityConfig {

    private final CustomUserDetailsService userDetailsService;
    private final CustomAuthenticationFailureHandler failureHandler;

    public SecurityConfig(CustomUserDetailsService userDetailsService,
                          CustomAuthenticationFailureHandler failureHandler) {
        this.userDetailsService = userDetailsService;
        this.failureHandler = failureHandler;
    }


    /** Thuật toán mã hóa mật khẩu BCrypt – đây là chuẩn hiện tại cho Spring Security. */
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    /**
     * Provider xác thực: kết nối UserDetailsService (load user từ DB)
     * với PasswordEncoder (so sánh mật khẩu).
     */
    @Bean
    public DaoAuthenticationProvider authenticationProvider() {
        DaoAuthenticationProvider provider = new DaoAuthenticationProvider(userDetailsService);
        provider.setPasswordEncoder(passwordEncoder());
        return provider;
    }

    /** AuthenticationManager dùng để thực hiện xác thực thủ công nếu cần. */
    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }



    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            // Tắt CSRF cho API chatbot và webhook SePay
            .csrf(csrf -> csrf.ignoringRequestMatchers("/api/chat/**", "/api/webhook/**", "/webhook/**"))

            .authenticationProvider(authenticationProvider())

            // Cấu hình phân quyền đường dẫn
            .authorizeHttpRequests(auth -> auth
                // File tĩnh và ảnh upload: ai cũng truy cập được
                .requestMatchers("/css/**", "/js/**", "/images/**", "/uploads/**",
                                 "/admin/css/**", "/admin/js/**", "/web/**", "/common/**","/error/**").permitAll()

                // Trang công khai: trang chủ, sản phẩm, chatbot, webhook, đăng ký/đăng nhập
                .requestMatchers("/", "/home", "/products/**", "/categories/**",
                                 "/brands/**", "/api/chat/**", "/api/webhook/**", "/webhook/**", "/api/orders/status/**", "/auth/**").permitAll()

                // Trang Admin: chỉ tài khoản có quyền ADMIN
                .requestMatchers("/admin/**").hasRole("ADMIN")

                // Tất cả còn lại (giỏ hàng, thanh toán, hồ sơ...): phải đăng nhập
                .anyRequest().authenticated()
            )

            // Cấu hình form đăng nhập
            .formLogin(form -> form
                .loginPage("/auth/login")                   // Trang hiển thị form đăng nhập
                .loginProcessingUrl("/auth/login-process")  // URL nhận dữ liệu submit từ form
                .defaultSuccessUrl("/", true)               // Đăng nhập thành công → về trang chủ
                .failureHandler(failureHandler)             // Handler xử lý lỗi đăng nhập tùy chỉnh
                .permitAll()
            )
            .exceptionHandling(e -> e.authenticationEntryPoint(new org.springframework.security.web.authentication.LoginUrlAuthenticationEntryPoint("/auth/login")))
            .sessionManagement(session -> session.invalidSessionUrl("/auth/login?expired=true"))
            
            // cấu hình rememberme
            .rememberMe(remember -> remember
                    .key("NuocHoaSecretKey_Dk93ns81")           // Khóa bí mật dùng để mã hóa token (bạn có thể đổi chuỗi này)
                    .rememberMeParameter("remember-me")         // Tên của checkbox trong form HTML (phải khớp với tên thẻ input)
                    .tokenValiditySeconds(7 * 24 * 60 * 60)     // Thời gian sống của cookie (7 ngày tính bằng giây)
                    .userDetailsService(userDetailsService)     // Cần thiết để Spring load lại User sau khi tắt trình duyệt
            )

            // Cấu hình đăng xuất
            .logout(logout -> logout
                .logoutUrl("/auth/logout")                      // URL kích hoạt đăng xuất
                .logoutSuccessUrl("/auth/login?logout=true")    // Sau khi đăng xuất → về trang login
                .invalidateHttpSession(true)                    // Hủy session phía server
                .deleteCookies("JSESSIONID", "remember-me") // Xóa cookie,remember-me phiên ở trình duyệt
                .clearAuthentication(true)
                .permitAll()
            );

        return http.build();
    }
}
