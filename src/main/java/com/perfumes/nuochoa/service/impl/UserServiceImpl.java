package com.perfumes.nuochoa.service.impl;

import com.perfumes.nuochoa.dto.RegisterRequest;
import com.perfumes.nuochoa.dto.UserAdminRequest;
import com.perfumes.nuochoa.dto.UserProfileRequest;
import com.perfumes.nuochoa.entity.Cart;
import com.perfumes.nuochoa.entity.Role;
import com.perfumes.nuochoa.entity.User;
import com.perfumes.nuochoa.entity.UserProfile;
import com.perfumes.nuochoa.repository.CartRepository;
import com.perfumes.nuochoa.repository.RoleRepository;
import com.perfumes.nuochoa.repository.UserProfileRepository;
import com.perfumes.nuochoa.repository.UserRepository;
import com.perfumes.nuochoa.service.UserService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.List;
import java.util.UUID;

@Service
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final UserProfileRepository userProfileRepository;
    private final CartRepository cartRepository;
    private final PasswordEncoder passwordEncoder;

    public UserServiceImpl(UserRepository userRepository,
                           RoleRepository roleRepository,
                           UserProfileRepository userProfileRepository,
                           CartRepository cartRepository,
                           PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
        this.userProfileRepository = userProfileRepository;
        this.cartRepository = cartRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    @Transactional
    public void registerUser(RegisterRequest registerRequest) {
        if (!registerRequest.getPassword().equals(registerRequest.getConfirmPassword())) {
            throw new IllegalArgumentException("Mật khẩu xác nhận không khớp");
        }

        if (userRepository.existsByUsername(registerRequest.getUsername())) {
            throw new IllegalArgumentException("Tên đăng nhập đã tồn tại trên hệ thống");
        }

        if (userRepository.existsByEmail(registerRequest.getEmail())) {
            throw new IllegalArgumentException("Email đã được sử dụng");
        }

        Role userRole = roleRepository.findByName("USER")
                .orElseThrow(() -> new RuntimeException("Lỗi hệ thống: Không tìm thấy quyền USER"));

        User user = new User();
        user.setUsername(registerRequest.getUsername());
        user.setEmail(registerRequest.getEmail());
        user.setPasswordHash(passwordEncoder.encode(registerRequest.getPassword()));
        user.setStatus("UNVERIFIED");
        user.setRole(userRole);

        User savedUser = userRepository.save(user);

        UserProfile userProfile = new UserProfile();
        userProfile.setUser(savedUser);
        userProfile.setLoyaltyPoints(0);
        userProfileRepository.save(userProfile);

        Cart cart = new Cart();
        cart.setUser(savedUser);
        cartRepository.save(cart);
    }

    @Override
    public User findByUsername(String username) {
        return userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy người dùng: " + username));
    }

    @Override
    public boolean existsByUsername(String username) {
        return userRepository.existsByUsername(username);
    }

    @Override
    public boolean existsByEmail(String email) {
        return userRepository.existsByEmail(email);
    }

    @Override
    public UserProfile getUserProfileByUserId(Long userId) {
        return userProfileRepository.findById(userId)
                .orElseGet(() -> {
                    User user = userRepository.findById(userId)
                            .orElseThrow(() -> new RuntimeException("Không tìm thấy User"));
                    UserProfile newProfile = new UserProfile();
                    newProfile.setUser(user);
                    newProfile.setLoyaltyPoints(0);
                    return userProfileRepository.save(newProfile);
                });
    }

    @Override
    @Transactional
    public void updateUserProfile(Long userId, UserProfileRequest profileRequest, MultipartFile avatarFile) {
        UserProfile profile = getUserProfileByUserId(userId);

        profile.setFullName(profileRequest.getFullName());
        profile.setPhone(profileRequest.getPhone());
        profile.setDateOfBirth(profileRequest.getDateOfBirth());
        profile.setGender(profileRequest.getGender());

        if (avatarFile != null && !avatarFile.isEmpty()) {
            String fileName = saveAvatarFile(avatarFile);
            profile.setAvatarUrl("/uploads/avatars/" + fileName);
        }

        userProfileRepository.save(profile);
    }

    private String saveAvatarFile(MultipartFile file) {
        try {
            String uploadDir = "uploads/avatars/";
            File dir = new File(uploadDir);
            if (!dir.exists()) {
                dir.mkdirs();
            }

            String fileName = UUID.randomUUID().toString() + "_" + file.getOriginalFilename();
            Path filePath = Paths.get(uploadDir + fileName);

            Files.copy(file.getInputStream(), filePath, StandardCopyOption.REPLACE_EXISTING);
            return fileName;
        } catch (IOException e) {
            throw new RuntimeException("Không thể lưu file ảnh!", e);
        }
    }

    @Override
    @Transactional
    public void enableUser(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy người dùng với email: " + email));
        user.setStatus("ACTIVE");
        userRepository.save(user);
    }

    @Override
    public User findByUsernameOrEmail(String identifier) {
        return userRepository.findByUsernameOrEmail(identifier, identifier)
                .orElseThrow(() -> new RuntimeException("Tên đăng nhập hoặc Email không tồn tại trên hệ thống!"));
    }

    @Override
    @Transactional
    public void updatePassword(String email, String newPassword) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy người dùng!"));
        user.setPasswordHash(passwordEncoder.encode(newPassword));
        userRepository.save(user);
    }

    @Override
    public List<User> getAllUsers() {
        return userRepository.findAll();
    }

    @Override
    @Transactional
    public void toggleUserStatus(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy người dùng!"));

        if ("ACTIVE".equalsIgnoreCase(user.getStatus())) {
            user.setStatus("LOCKED");
        } else if ("LOCKED".equalsIgnoreCase(user.getStatus())) {
            user.setStatus("ACTIVE");
        }
        userRepository.save(user);
    }

    @Override
    public User getUserById(Long id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy tài khoản với ID: " + id));
    }

    @Override
    @Transactional
    public void createUserByAdmin(UserAdminRequest request) {
        if (userRepository.existsByUsername(request.getUsername())) {
            throw new RuntimeException("Tên đăng nhập đã tồn tại!");
        }
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new RuntimeException("Email đã được sử dụng!");
        }

        User user = new User();
        user.setUsername(request.getUsername());
        user.setEmail(request.getEmail());
        user.setPasswordHash(passwordEncoder.encode(request.getPassword()));
        user.setStatus(request.getStatus() != null ? request.getStatus() : "ACTIVE");

        Role role = roleRepository.findByName(request.getRole() != null ? request.getRole() : "USER")
                .orElseThrow(() -> new RuntimeException("Không tìm thấy quyền hệ thống!"));
        user.setRole(role); // Sửa từ setRoles(Set.of(role)) -> setRole(role)

        userRepository.save(user);
    }

    @Override
    @Transactional
    public void updateUserByAdmin(Long id, UserAdminRequest request) {
        User user = getUserById(id);

        user.setEmail(request.getEmail());
        user.setStatus(request.getStatus());

        if (request.getPassword() != null && !request.getPassword().trim().isEmpty()) {
            user.setPasswordHash(passwordEncoder.encode(request.getPassword()));
        }

        Role role = roleRepository.findByName(request.getRole())
                .orElseThrow(() -> new RuntimeException("Không tìm thấy quyền!"));
        user.setRole(role); // Sửa từ setRoles(Set.of(role)) -> setRole(role)

        userRepository.save(user);
    }

    @Override
    @Transactional
    public void deleteUser(Long id) {
        if (!userRepository.existsById(id)) {
            throw new RuntimeException("Tài khoản không tồn tại!");
        }
        userRepository.deleteById(id);
    }
}