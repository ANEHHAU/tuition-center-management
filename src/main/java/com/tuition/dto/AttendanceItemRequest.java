package com.tuition.dto;

import com.tuition.entity.AttendanceStatus;
import jakarta.validation.constraints.NotNull;

public record AttendanceItemRequest(
        @NotNull(message = "Mã học sinh không được để trống")
        Long studentId,

        @NotNull(message = "Trạng thái điểm danh không được để trống")
        AttendanceStatus status,

        String note
) {
}
