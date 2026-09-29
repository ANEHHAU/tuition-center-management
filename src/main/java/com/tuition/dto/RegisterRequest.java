package com.tuition.dto;

import com.tuition.entity.Role;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record RegisterRequest(
        @NotBlank(message = "Tên đăng nhập không được để trống")
        String username,

        @NotBlank(message = "Mật khẩu không được để trống")
        @Size(min = 6, message = "Mật khẩu phải chứa ít nhất 6 ký tự")
        String password,

        @NotNull(message = "Role không được để trống")
        Role role,

        @NotBlank(message = "Họ và tên không được để trống")
        String fullName,

        @Email(message = "Email không đúng định dạng")
        String email,

        String phone,
        String address,
        String note,
        String avatarUrl
) {
}
