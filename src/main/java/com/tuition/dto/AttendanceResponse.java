package com.tuition.dto;

import com.tuition.entity.AttendanceStatus;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/**
 * Response trả về danh sách điểm danh của 1 session
 */
public record AttendanceResponse(
        Long sessionId,
        List<AttendanceItem> items
) {
    public record AttendanceItem(
            Long studentId,
            String studentName,
            AttendanceStatus status,
            String note,
            LocalDateTime recordedAt,
            BigDecimal priceSnapshot
    ) {
    }
}
