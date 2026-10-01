package com.tuition.dto;

import com.tuition.entity.User;
import com.tuition.entity.UserStatus;

import java.util.List;

public record StudentWithGroupsResponse(
        Long id,
        String fullName,
        String email,
        String phone,
        UserStatus status,
        List<String> enrolledGroups
) {
    public static StudentWithGroupsResponse fromEntity(User user, List<String> groups) {
        return new StudentWithGroupsResponse(
                user.getId(),
                user.getFullName(),
                user.getEmail(),
                user.getPhone(),
                user.getStatus(),
                groups
        );
    }
}
