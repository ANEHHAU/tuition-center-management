package com.tuition.dto;

import com.tuition.entity.AttendanceStatus;
import com.tuition.entity.InvoiceDetail;

import java.math.BigDecimal;
import java.time.LocalDate;

public record InvoiceDetailResponse(
        Long id,
        String courseName,
        LocalDate sessionDate,
        BigDecimal price,
        AttendanceStatus status
) {
    public static InvoiceDetailResponse fromEntity(InvoiceDetail detail) {
        return new InvoiceDetailResponse(
                detail.getId(),
                detail.getCourse().getName(),
                detail.getSessionDate(),
                detail.getPrice(),
                detail.getStatus()
        );
    }
}
