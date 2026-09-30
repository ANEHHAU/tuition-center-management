package com.tuition.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

public record RevenueReportResponse(
        LocalDateTime fromDate,
        LocalDateTime toDate,
        BigDecimal totalRevenue,
        List<TeacherRevenue> byTeacher
) {
    public record TeacherRevenue(
            Long teacherId,
            String teacherName,
            BigDecimal revenue
    ) {
    }
}
