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

    // Inject cả userDetailsService và failureHandler mới tạo
    public SecurityConfig(CustomUserDetailsService userDetailsService,
                          CustomAuthenticationFailureHandler failureHandler) {
        this.userDetailsService = userDetailsService;
        this.failureHandler = failureHandler;
    }

    // Thuật toán mã hóa mật khẩu BCrypt
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public DaoAuthenticationProvider authenticationProvider() {
        DaoAuthenticationProvider authProvider = new DaoAuthenticationProvider(userDetailsService);
        authProvider.setPasswordEncoder(passwordEncoder());
        return authProvider;
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration authConfig) throws Exception {
        return authConfig.getAuthenticationManager();
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                // 1. Tắt CSRF cho API Chatbot để JavaScript gửi POST request thành công
                .csrf(csrf -> csrf.ignoringRequestMatchers("/api/chat/**"))

                .authenticationProvider(authenticationProvider())
                .authorizeHttpRequests(auth -> auth
                        // 2. Công khai các thư mục chứa file tĩnh & thư mục upload ảnh
                        .requestMatchers("/css/**", "/js/**", "/images/**", "/uploads/**", "/admin/css/**", "/admin/js/**", "/web/**", "/common/**").permitAll()

                        // 3. THÊM "/api/chat/**" VÀO ĐÂY để ai cũng có thể nhắn tin với AI
                        .requestMatchers("/", "/home", "/products/**", "/categories/**", "/brands/**", "/api/chat/**").permitAll()
                        .requestMatchers("/auth/**").permitAll()

                        // 4. Phân quyền đường dẫn Admin
                        .requestMatchers("/admin/**").hasRole("ADMIN")

                        // 5. Các chức năng cần đăng nhập (Giỏ hàng, Thanh toán, Hồ sơ)
                        .anyRequest().authenticated()
                )
                .formLogin(form -> form
                        .loginPage("/auth/login")                  // Trang hiển thị form Đăng nhập
                        .loginProcessingUrl("/auth/login-process") // URL nhận dữ liệu submit từ form
                        .defaultSuccessUrl("/", true)              // Đăng nhập thành công chuyển về Trang chủ
                        .failureHandler(failureHandler)            // Sử dụng Failure Handler xử lý tài khoản UNVERIFIED
                        .permitAll()
                )
                .logout(logout -> logout
                        .logoutUrl("/auth/logout")                 // URL kích hoạt Đăng xuất
                        .logoutSuccessUrl("/auth/login?logout=true")// Đăng xuất xong chuyển về trang Login
                        .invalidateHttpSession(true)               // Hủy hoàn toàn Session phía Server
                        .deleteCookies("JSESSIONID")               // Xóa Cookie đăng nhập ở trình duyệt
                        .clearAuthentication(true)
                        .permitAll()
                );

        return http.build();
    }
}