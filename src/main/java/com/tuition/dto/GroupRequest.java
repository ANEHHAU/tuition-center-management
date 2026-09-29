package com.tuition.dto;

import com.tuition.entity.GroupStatus;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;

public record GroupRequest(
        @NotBlank(message = "Tên nhóm không được để trống")
        @Size(max = 100, message = "Tên nhóm tối đa 100 ký tự")
        String name,

        @NotNull(message = "Mã khóa học không được để trống")
        Long courseId,

        LocalDate startDate,

        LocalDate endDate,

        GroupStatus status
) {
}
