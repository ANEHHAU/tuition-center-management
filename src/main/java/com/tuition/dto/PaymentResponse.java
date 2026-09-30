package com.tuition.dto;

import com.tuition.entity.Payment;
import com.tuition.entity.PaymentMethod;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record PaymentResponse(
        Long id,
        Long invoiceId,
        BigDecimal amount,
        PaymentMethod method,
        LocalDateTime paidAt,
        String recordedByName,
        String note
) {
    public static PaymentResponse fromEntity(Payment payment) {
        return new PaymentResponse(
                payment.getId(),
                payment.getInvoice().getId(),
                payment.getAmount(),
                payment.getMethod(),
                payment.getPaidAt(),
                payment.getRecordedBy() != null ? payment.getRecordedBy().getFullName() : null,
                payment.getNote()
        );
    }
}
