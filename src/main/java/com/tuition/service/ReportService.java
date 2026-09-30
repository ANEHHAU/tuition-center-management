package com.tuition.service;

import com.tuition.dto.DebtReportResponse;
import com.tuition.dto.RevenueReportResponse;
import com.tuition.entity.Invoice;
import com.tuition.entity.InvoiceStatus;
import com.tuition.entity.User;
import com.tuition.repository.InvoiceRepository;
import com.tuition.repository.PaymentRepository;
import com.tuition.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ReportService {

    private final PaymentRepository paymentRepository;
    private final InvoiceRepository invoiceRepository;
    private final UserRepository userRepository;

    @Transactional(readOnly = true)
    public RevenueReportResponse revenueByTeacher(Long teacherId, LocalDateTime from, LocalDateTime to) {
        BigDecimal total;
        List<RevenueReportResponse.TeacherRevenue> teacherRevenues = new ArrayList<>();
        
        if (teacherId == null) {
            total = paymentRepository.sumAmountByPaidAtBetween(from, to);
        } else {
            total = paymentRepository.sumAmountByTeacherAndPaidAtBetween(teacherId, from, to);
            User teacher = userRepository.findById(teacherId).orElseThrow();
            teacherRevenues.add(new RevenueReportResponse.TeacherRevenue(teacherId, teacher.getFullName(), total != null ? total : BigDecimal.ZERO));
        }
        
        if (total == null) total = BigDecimal.ZERO;
        
        return new RevenueReportResponse(from, to, total, teacherRevenues);
    }

    @Transactional(readOnly = true)
    public List<DebtReportResponse> debtReport(Long teacherId) {
        List<InvoiceStatus> debtStatuses = List.of(InvoiceStatus.UNPAID, InvoiceStatus.PARTIAL, InvoiceStatus.OVERDUE);
        List<Invoice> unpaidInvoices = (teacherId != null) 
                ? invoiceRepository.findByTeacherIdAndStatusIn(teacherId, debtStatuses)
                : invoiceRepository.findByStatusIn(debtStatuses);

        Map<User, List<Invoice>> grouped = unpaidInvoices.stream().collect(Collectors.groupingBy(Invoice::getStudent));

        List<DebtReportResponse> report = new ArrayList<>();
        for (Map.Entry<User, List<Invoice>> entry : grouped.entrySet()) {
            User student = entry.getKey();
            List<Invoice> invoices = entry.getValue();
            
            BigDecimal totalDebt = invoices.stream()
                    .map(i -> i.getTotalAmount().subtract(i.getPaidAmount()))
                    .reduce(BigDecimal.ZERO, BigDecimal::add);
                    
            // Find oldest month
            Invoice oldest = invoices.stream().min((i1, i2) -> {
                if (!i1.getYear().equals(i2.getYear())) return i1.getYear().compareTo(i2.getYear());
                return i1.getMonth().compareTo(i2.getMonth());
            }).orElse(null);
            
            String oldestMonthStr = oldest != null ? oldest.getMonth() + "/" + oldest.getYear() : "";
            
            report.add(new DebtReportResponse(student.getId(), student.getFullName(), totalDebt, oldestMonthStr, invoices.size()));
        }

        return report;
    }
}
