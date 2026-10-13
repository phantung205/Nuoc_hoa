package com.perfumes.nuochoa.security;

import com.perfumes.nuochoa.entity.User;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.List;


public class CustomUserDetails implements UserDetails {

    private final User user;

    public CustomUserDetails(User user) {
        this.user = user;
    }

    public User getUser() {
        return user;
    }

    // hàm này cấp quyền cho người dùng đăng nhập thêm tiền tố role là bắt buộc
    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        String roleName = "ROLE_" + user.getRole().getName();
        return List.of(new SimpleGrantedAuthority(roleName));
    }

    @Override
    public String getPassword() {
        return user.getPasswordHash();
    }

    @Override
    public String getUsername() {
        return user.getUsername();
    }

    // hàm này kiểm tra xem tài khoản hết hạn hay ko bạn để mặc định là true ko bh hết hạn
    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    // trạng thái của tài khóa trả về false chặn dăng nhập
    @Override
    public boolean isAccountNonLocked() {
        return !"LOCKED".equalsIgnoreCase(user.getStatus());
    }

    // kiểm tra mật khẩu có hết hạn hay ko bạn để true là ko bbao h hết
    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    // kiểm tra xem tài khoản này đã được kích hoạt hay chưa nếu chưa trả về false ném ngoại lệ DisabledException chuyêbr sang failurehander đẻ sử lý lỗi
    @Override
    public boolean isEnabled() {
        return "ACTIVE".equalsIgnoreCase(user.getStatus());
    }
}