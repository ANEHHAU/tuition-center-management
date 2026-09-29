package com.tuition.service;

import com.tuition.dto.ChangePasswordRequest;
import com.tuition.dto.UpdateProfileRequest;
import com.tuition.dto.UserResponse;
import com.tuition.entity.User;
import com.tuition.exception.BusinessException;
import com.tuition.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Service quản lý thông tin cá nhân và đổi mật khẩu người dùng.
 */
@Service
@RequiredArgsConstructor
public class ProfileService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    /**
     * Lấy thông tin hồ sơ cá nhân theo username
     */
    public UserResponse getProfile(String username) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new BusinessException("Người dùng không tồn tại"));
        return UserResponse.fromEntity(user);
    }

    /**
     * Cập nhật thông tin cá nhân
     */
    @Transactional
    public UserResponse updateProfile(String username, UpdateProfileRequest req) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new BusinessException("Người dùng không tồn tại"));

        // Kiểm tra email nếu người dùng thay đổi email
        if (req.email() != null && !req.email().isBlank() && !req.email().equalsIgnoreCase(user.getEmail())) {
            if (userRepository.existsByEmail(req.email())) {
                throw new BusinessException("Email đã được sử dụng bởi tài khoản khác");
            }
            user.setEmail(req.email());
        }

        if (req.fullName() != null && !req.fullName().isBlank()) {
            user.setFullName(req.fullName());
        }

        if (req.phone() != null) {
            user.setPhone(req.phone());
        }

        if (req.address() != null) {
            user.setAddress(req.address());
        }

        if (req.note() != null) {
            user.setNote(req.note());
        }

        if (req.avatarUrl() != null) {
            user.setAvatarUrl(req.avatarUrl());
        }

        User updatedUser = userRepository.save(user);
        return UserResponse.fromEntity(updatedUser);
    }

    /**
     * Đổi mật khẩu tài khoản
     * Quy tắc: BẮT BUỘC kiểm tra mật khẩu cũ chính xác trước khi cho phép đổi.
     */
    @Transactional
    public void changePassword(String username, ChangePasswordRequest req) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new BusinessException("Người dùng không tồn tại"));

        if (!passwordEncoder.matches(req.oldPassword(), user.getPassword())) {
            throw new BusinessException("Mật khẩu cũ không chính xác");
        }

        user.setPassword(passwordEncoder.encode(req.newPassword()));
        userRepository.save(user);
    }
}
