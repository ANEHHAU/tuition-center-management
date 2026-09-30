package com.tuition.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;

import java.util.List;

public record AttendanceRequest(
        @NotEmpty(message = "Danh sách điểm danh không được rỗng")
        @Valid
        List<AttendanceItemRequest> items
) {
}
