package com.tuition.dto;

import com.tuition.entity.CourseStatus;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;

public record CourseRequest(
        @NotBlank(message = "Tên khóa học không được để trống")
        @Size(max = 150, message = "Tên khóa học tối đa 150 ký tự")
        String name,

        @NotNull(message = "Giá mỗi buổi không được để trống")
        @DecimalMin(value = "0.01", message = "Giá mỗi buổi phải lớn hơn 0")
        BigDecimal pricePerSession,

        String description,

        String coverUrl,

        String coverPublicId,

        CourseStatus status,

        // Chỉ ADMIN mới dùng field này để gán course cho teacher khác
        Long teacherId
) {
}
