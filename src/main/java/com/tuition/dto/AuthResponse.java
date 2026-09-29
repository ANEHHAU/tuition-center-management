package com.tuition.dto;

import com.tuition.entity.Role;

public record AuthResponse(
        String token,
        String username,
        Role role,
        Long userId,
        String fullName,
        String avatarUrl
) {
}
