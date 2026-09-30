package com.tuition.dto;

import com.tuition.entity.Invoice;
import com.tuition.entity.InvoiceDocumentStatus;
import com.tuition.entity.InvoiceStatus;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

public record InvoiceResponse(
        Long id,
        Long studentId,
        String studentName,
        Integer month,
        Integer year,
        Integer totalSessions,
        BigDecimal totalAmount,
        BigDecimal paidAmount,
        BigDecimal remainingAmount,
        InvoiceStatus status,
        InvoiceDocumentStatus invoiceStatus,
        Boolean needRegenerate,
        LocalDateTime generatedAt,
        String note,
        List<InvoiceDetailResponse> details
) {
    public static InvoiceResponse fromEntity(Invoice invoice) {
        BigDecimal remaining = invoice.getTotalAmount().subtract(invoice.getPaidAmount());
        
        List<InvoiceDetailResponse> detailResponses = null;
        if (invoice.getDetails() != null) {
            detailResponses = invoice.getDetails().stream()
                    .map(InvoiceDetailResponse::fromEntity)
                    .collect(Collectors.toList());
        }

        return new InvoiceResponse(
                invoice.getId(),
                invoice.getStudent().getId(),
                invoice.getStudent().getFullName(),
                invoice.getMonth(),
                invoice.getYear(),
                invoice.getTotalSessions(),
                invoice.getTotalAmount(),
                invoice.getPaidAmount(),
                remaining,
                invoice.getStatus(),
                invoice.getInvoiceStatus(),
                invoice.getNeedRegenerate(),
                invoice.getGeneratedAt(),
                invoice.getNote(),
                detailResponses
        );
    }
}
