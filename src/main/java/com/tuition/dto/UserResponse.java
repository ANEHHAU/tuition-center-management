package com.tuition.dto;

import com.tuition.entity.Role;
import com.tuition.entity.User;
import com.tuition.entity.UserStatus;

import java.time.LocalDateTime;

public record UserResponse(
        Long id,
        String username,
        Role role,
        String fullName,
        String email,
        String phone,
        String address,
        String note,
        String avatarUrl,
        UserStatus status,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
    public static UserResponse fromEntity(User user) {
        if (user == null) {
            return null;
        }
        return new UserResponse(
                user.getId(),
                user.getUsername(),
                user.getRole(),
                user.getFullName(),
                user.getEmail(),
                user.getPhone(),
                user.getAddress(),
                user.getNote(),
                user.getAvatarUrl(),
                user.getStatus(),
                user.getCreatedAt(),
                user.getUpdatedAt()
        );
    }
}
