package com.tuition.dto;

import com.tuition.entity.PaymentMethod;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.math.BigDecimal;

public record PaymentRequest(
        @NotNull(message = "Mã hóa đơn không được để trống")
        Long invoiceId,

        @NotNull(message = "Số tiền không được để trống")
        @Positive(message = "Số tiền phải lớn hơn 0")
        BigDecimal amount,

        @NotNull(message = "Phương thức thanh toán không được để trống")
        PaymentMethod method,

        String note
) {
}
