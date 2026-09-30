package com.tuition.dto;

import java.math.BigDecimal;

public record DebtReportResponse(
        Long studentId,
        String studentName,
        BigDecimal totalDebt,
        String oldestUnpaidMonth,
        Integer invoiceCount
) {
}
