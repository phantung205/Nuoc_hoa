package com.perfumes.nuochoa.service;

import com.perfumes.nuochoa.dto.RegisterRequest;
import com.perfumes.nuochoa.dto.UserAdminRequest;
import com.perfumes.nuochoa.dto.UserProfileRequest;
import com.perfumes.nuochoa.entity.User;
import com.perfumes.nuochoa.entity.UserProfile;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;


public interface UserService {

    void registerUser(RegisterRequest registerRequest);

    void enableUser(String email);

    User findByUsernameOrEmail(String identifier);

    void updatePassword(String email, String newPassword);

    UserProfile getUserProfileByUserId(Long userId);

    void updateUserProfile(Long userId, UserProfileRequest profileRequest, MultipartFile avatarFile);


    User findByUsername(String username);

    boolean existsByUsername(String username);

    boolean existsByEmail(String email);

    List<User> getAllUsers();

    long countAllUsers();

    void toggleUserStatus(Long userId);

    User getUserById(Long id);

    void createUserByAdmin(UserAdminRequest request);

    void updateUserByAdmin(Long id, UserAdminRequest request);

    void deleteUser(Long id);
}