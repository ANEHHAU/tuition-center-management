package com.tuition.dto;

import jakarta.validation.constraints.Email;

public record UpdateUserRequest(
        String fullName,

        @Email(message = "Email không đúng định dạng")
        String email,

        String phone,
        String address,
        String note,
        String avatarUrl
) {
}
