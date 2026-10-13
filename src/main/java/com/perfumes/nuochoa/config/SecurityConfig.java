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


    // bộ mã hóa mật khẩu
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    // đây là cầu nối giữa quá trình kiểm duyện và databasse
    @Bean
    public DaoAuthenticationProvider authenticationProvider() {
        // lấy thông tin user từ database
        // khi đăng nhập provider sẽ gọi đến UserDetailsService tìm user thực hiện các hàm kiểm tra
        DaoAuthenticationProvider provider = new DaoAuthenticationProvider(userDetailsService);
        // cung cấp công cụ đêr so bảo mật
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
            // Tắt CSRF cho API chatbot, webhook SePay, wishlist và review
            .csrf(csrf -> csrf.ignoringRequestMatchers("/api/chat/**", "/api/webhook/**", "/webhook/**", "/api/wishlist/**", "/api/reviews/**"))

            .authenticationProvider(authenticationProvider())

            // Cấu hình phân quyền đường dẫn
            .authorizeHttpRequests(auth -> auth
                // File tĩnh và ảnh upload: ai cũng truy cập được
                .requestMatchers("/css/**", "/js/**", "/images/**", "/uploads/**",
                                 "/admin/css/**", "/admin/js/**", "/web/**", "/common/**","/error/**").permitAll()

                // Trang công khai: trang chủ, sản phẩm, chatbot, webhook, đăng ký/đăng nhập
                .requestMatchers("/", "/home", "/products/**", "/categories/**",
                                 "/brands/**", "/api/chat/**", "/api/webhook/**", "/webhook/**", "/api/orders/status/**", "/auth/**", "/api/wishlist/**", "/api/reviews/**").permitAll()


                .requestMatchers("/admin/**").hasRole("ADMIN")
                .requestMatchers("/staff/**").hasAnyRole("ADMIN", "STAFF")

                // Tất cả còn lại (giỏ hàng, thanh toán, hồ sơ...): phải đăng nhập
                .anyRequest().authenticated()
            )

            // Cấu hình form đăng nhập
            .formLogin(form -> form
                .loginPage("/auth/login")
                .loginProcessingUrl("/auth/login-process")
                .defaultSuccessUrl("/", true)
                .failureHandler(failureHandler)
                .permitAll()
            )
            .exceptionHandling(e -> e.authenticationEntryPoint(new org.springframework.security.web.authentication.LoginUrlAuthenticationEntryPoint("/auth/login")))
            .sessionManagement(session -> session.invalidSessionUrl("/auth/login?expired=true"))
            
            // cấu hình rememberme
            .rememberMe(remember -> remember
                    .key("NuocHoaSecretKey_Dk93ns81")           // Khóa bí mật dùng để mã hóa token
                    .rememberMeParameter("remember-me")
                    .tokenValiditySeconds(7 * 24 * 60 * 60)
                    .userDetailsService(userDetailsService)     // spring load lại user
            )

            // Cấu hình đăng xuất
            .logout(logout -> logout
                .logoutUrl("/auth/logout")                      // url kích hoạt đăng xuất
                .logoutSuccessUrl("/auth/login?logout=true")    // Sau khi đăng xuất → về trang login
                .invalidateHttpSession(true)                    // Hủy session phía server
                .deleteCookies("JSESSIONID", "remember-me") // Xóa cookie,remember-me phiên ở trình duyệt
                .clearAuthentication(true)
                .permitAll()
            );

        return http.build();
    }
}
