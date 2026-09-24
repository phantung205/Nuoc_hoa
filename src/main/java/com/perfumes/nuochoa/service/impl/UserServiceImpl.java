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
        // 1. Kiểm tra mật khẩu xác nhận
        if (!registerRequest.getPassword().equals(registerRequest.getConfirmPassword())) {
            throw new IllegalArgumentException("Mật khẩu xác nhận không khớp");
        }

        // 2. Kiểm tra trùng lặp
        if (userRepository.existsByUsername(registerRequest.getUsername())) {
            throw new IllegalArgumentException("Tên đăng nhập đã tồn tại trên hệ thống");
        }
        if (userRepository.existsByEmail(registerRequest.getEmail())) {
            throw new IllegalArgumentException("Email đã được sử dụng");
        }

        // 3. Tạo User mới với trạng thái UNVERIFIED (chưa xác thực OTP)
        Role userRole = roleRepository.findByName("USER")
                .orElseThrow(() -> new RuntimeException("Lỗi hệ thống: Không tìm thấy quyền USER"));

        User newUser = new User();
        newUser.setUsername(registerRequest.getUsername());
        newUser.setEmail(registerRequest.getEmail());
        newUser.setPasswordHash(passwordEncoder.encode(registerRequest.getPassword()));
        newUser.setStatus("UNVERIFIED");
        newUser.setRole(userRole);

        User savedUser = userRepository.save(newUser);

        // 4. Tạo Profile rỗng – loyaltyPoints mặc định = 0
        UserProfile profile = new UserProfile();
        profile.setUser(savedUser);
        userProfileRepository.save(profile);

        // 5. Tạo Cart rỗng để người dùng có thể thêm sản phẩm ngay sau khi đăng nhập
        Cart cart = new Cart();
        cart.setUser(savedUser);
        cartRepository.save(cart);
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
    public UserProfile getUserProfileByUserId(Long userId) {
        return userProfileRepository.findById(userId)
                .orElseGet(() -> createEmptyProfileForUser(userId));
    }

    /** Tạo UserProfile rỗng cho User chưa có Profile. */
    private UserProfile createEmptyProfileForUser(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy User với ID: " + userId));

        UserProfile newProfile = new UserProfile();
        newProfile.setUser(user);
        return userProfileRepository.save(newProfile);
    }

    @Override
    @Transactional
    public void updateUserProfile(Long userId, UserProfileRequest profileRequest, MultipartFile avatarFile) {
        UserProfile profile = getUserProfileByUserId(userId);

        // Cập nhật các thông tin cơ bản từ form
        profile.setFullName(profileRequest.getFullName());
        profile.setPhone(profileRequest.getPhone());
        profile.setDateOfBirth(profileRequest.getDateOfBirth());
        profile.setGender(profileRequest.getGender());

        // Nếu người dùng có chọn ảnh mới thì lưu ảnh và cập nhật đường dẫn
        if (avatarFile != null && !avatarFile.isEmpty()) {
            // Xóa ảnh cũ cứng trên ổ đĩa
            if (profile.getAvatarUrl() != null && !profile.getAvatarUrl().isEmpty()) {
                deleteFile(profile.getAvatarUrl());
            }

            String savedFileName = saveAvatarFile(avatarFile);
            profile.setAvatarUrl("/uploads/avatars/" + savedFileName);
        }

        userProfileRepository.save(profile);
    }

    private String saveAvatarFile(MultipartFile file) {
        try {
            String uploadDir = "uploads/avatars/";

            // Tạo thư mục nếu chưa tồn tại
            File dir = new File(uploadDir);
            if (!dir.exists()) {
                dir.mkdirs();
            }
            // Tạo tên file duy nhất: UUID + tên gốc (để tránh ghi đè file khác)
            String uniqueFileName = UUID.randomUUID() + "_" + file.getOriginalFilename();
            Path targetPath = Paths.get(uploadDir + uniqueFileName);

            Files.copy(file.getInputStream(), targetPath, StandardCopyOption.REPLACE_EXISTING);
            return uniqueFileName;

        } catch (IOException e) {
            throw new RuntimeException("Không thể lưu file ảnh đại diện!", e);
        }
    }

    private void deleteFile(String fileUrl) {
        try {
            if (fileUrl != null && fileUrl.startsWith("/")) {
                Path path = Paths.get(fileUrl.substring(1)); // Bỏ dấu / ở đầu (vd: /uploads/... -> uploads/...)
                Files.deleteIfExists(path);
            }
        } catch (IOException e) {
            System.err.println("Không thể xóa file: " + fileUrl);
        }
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
    public List<User> getAllUsers() {
        return userRepository.findAll();
    }


    @Override
    public long countAllUsers() {
        return userRepository.count();
    }


    @Override
    @Transactional
    public void toggleUserStatus(Long userId) {
        User user = getUserById(userId);

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
    public void createUserByAdmin(UserAdminRequest request, MultipartFile avatarFile) {
        if (userRepository.existsByUsername(request.getUsername())) {
            throw new RuntimeException("Tên đăng nhập đã tồn tại!");
        }
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new RuntimeException("Email đã được sử dụng!");
        }

        // Lấy role: dùng role được chọn, nếu không chọn thì mặc định là USER
        String roleName = (request.getRole() != null) ? request.getRole() : "USER";
        Role role = roleRepository.findByName(roleName)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy quyền hệ thống: " + roleName));

        User newUser = new User();
        newUser.setUsername(request.getUsername());
        newUser.setEmail(request.getEmail());
        newUser.setPasswordHash(passwordEncoder.encode(request.getPassword()));
        newUser.setStatus((request.getStatus() != null) ? request.getStatus() : "ACTIVE");
        newUser.setRole(role);

        User savedUser = userRepository.save(newUser);

        // Tạo profile
        UserProfile profile = new UserProfile();
        profile.setUser(savedUser);
        profile.setFullName(request.getFullName());
        profile.setPhone(request.getPhone());
        profile.setDateOfBirth(request.getDateOfBirth());
        profile.setGender(request.getGender());

        if (avatarFile != null && !avatarFile.isEmpty()) {
            String savedFileName = saveAvatarFile(avatarFile);
            profile.setAvatarUrl("/uploads/avatars/" + savedFileName);
        }

        userProfileRepository.save(profile);

        // Tạo cart
        Cart cart = new Cart();
        cart.setUser(savedUser);
        cartRepository.save(cart);
    }


    @Override
    @Transactional
    public void updateUserByAdmin(Long id, UserAdminRequest request, MultipartFile avatarFile) {
        User user = getUserById(id);

        user.setEmail(request.getEmail());
        user.setStatus(request.getStatus());

        // Chỉ đổi mật khẩu nếu Admin nhập mật khẩu mới
        boolean hasNewPassword = request.getPassword() != null && !request.getPassword().trim().isEmpty();
        if (hasNewPassword) {
            user.setPasswordHash(passwordEncoder.encode(request.getPassword()));
        }

        Role role = roleRepository.findByName(request.getRole())
                .orElseThrow(() -> new RuntimeException("Không tìm thấy quyền: " + request.getRole()));
        user.setRole(role);

        userRepository.save(user);

        // Lấy và cập nhật profile
        UserProfile profile = getUserProfileByUserId(id);
        profile.setFullName(request.getFullName());
        profile.setPhone(request.getPhone());
        profile.setDateOfBirth(request.getDateOfBirth());
        profile.setGender(request.getGender());

        if (avatarFile != null && !avatarFile.isEmpty()) {
            if (profile.getAvatarUrl() != null && !profile.getAvatarUrl().isEmpty()) {
                deleteFile(profile.getAvatarUrl());
            }
            String savedFileName = saveAvatarFile(avatarFile);
            profile.setAvatarUrl("/uploads/avatars/" + savedFileName);
        }

        userProfileRepository.save(profile);
    }

    @Override
    @Transactional
    public void deleteUser(Long id) {
        User user = userRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("Tài khoản không tồn tại!"));
            
        // Xóa ảnh cứng của User (nếu có)
        userProfileRepository.findById(id).ifPresent(profile -> {
            if (profile.getAvatarUrl() != null && !profile.getAvatarUrl().isEmpty()) {
                deleteFile(profile.getAvatarUrl());
            }
        });
        
        userRepository.delete(user);
    }
}