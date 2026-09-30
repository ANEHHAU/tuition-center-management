package com.tuition.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

import java.util.List;

public record GenerateInvoiceRequest(
        @NotNull(message = "Tháng không được để trống")
        @Min(value = 1, message = "Tháng không hợp lệ")
        @Max(value = 12, message = "Tháng không hợp lệ")
        Integer month,

        @NotNull(message = "Năm không được để trống")
        @Min(value = 2000, message = "Năm không hợp lệ")
        Integer year,

        Long teacherId,
        List<Long> studentIds
) {
}
