package com.tuition.dto;

import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;

public record EnrollmentRequest(
        @NotNull(message = "Mã học sinh không được để trống")
        Long studentId,

        @NotNull(message = "Mã nhóm không được để trống")
        Long groupId,

        LocalDate joinDate
) {
}
