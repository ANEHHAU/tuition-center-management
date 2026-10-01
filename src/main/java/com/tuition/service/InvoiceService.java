package com.tuition.service;

import com.tuition.aspect.Auditable;
import com.tuition.dto.InvoiceResponse;
import com.tuition.entity.*;
import com.tuition.exception.BusinessException;
import com.tuition.repository.AttendanceRepository;
import com.tuition.repository.InvoiceDetailRepository;
import com.tuition.repository.InvoiceRepository;
import com.tuition.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class InvoiceService {

    private final InvoiceRepository invoiceRepository;
    private final InvoiceDetailRepository invoiceDetailRepository;
    private final AttendanceRepository attendanceRepository;
    private final UserRepository userRepository;

    /**
     * Tạo hóa đơn cho 1 học sinh theo tháng/năm
     */
    @Transactional
    @Auditable(action = "GENERATE", entityType = "INVOICE")
    public InvoiceResponse generateForStudent(Long studentId, Integer month, Integer year, User currentUser) {
        User student = userRepository.findById(studentId)
                .orElseThrow(() -> new BusinessException("Không tìm thấy học sinh"));

        // Kiểm tra xem đã có hóa đơn chưa
        Optional<Invoice> existingInvoice = invoiceRepository.findByStudentIdAndMonthAndYear(studentId, month, year);
        if (existingInvoice.isPresent()) {
            throw new BusinessException("Hóa đơn cho học sinh này trong tháng " + month + "/" + year + " đã tồn tại.");
        }

        return createOrUpdateInvoice(student, month, year, currentUser, new Invoice());
    }

    /**
     * Tái tạo lại hóa đơn (Regenerate)
     */
    @Transactional
    @Auditable(action = "REGENERATE", entityType = "INVOICE")
    public InvoiceResponse regenerate(Long invoiceId, User currentUser) {
        Invoice invoice = invoiceRepository.findById(invoiceId)
                .orElseThrow(() -> new BusinessException("Không tìm thấy hóa đơn"));
        
        if (invoice.getInvoiceStatus() == InvoiceDocumentStatus.FINALIZED) {
            throw new BusinessException("Không thể tính lại hóa đơn đã được chốt (FINALIZED).");
        }

        // Xóa chi tiết cũ
        invoice.getDetails().clear();
        invoiceDetailRepository.deleteAll(invoiceDetailRepository.findByInvoiceId(invoiceId));

        return createOrUpdateInvoice(invoice.getStudent(), invoice.getMonth(), invoice.getYear(), currentUser, invoice);
    }

    private InvoiceResponse createOrUpdateInvoice(User student, Integer month, Integer year, User currentUser, Invoice invoice) {
        // Tìm ngày đầu tháng và cuối tháng
        LocalDate fromDate = LocalDate.of(year, month, 1);
        LocalDate toDate = fromDate.withDayOfMonth(fromDate.lengthOfMonth());

        // Lấy tất cả attendance của HS trong tháng
        List<Attendance> attendances = attendanceRepository.findByStudentIdAndSessionDateBetween(student.getId(), fromDate, toDate);

        BigDecimal totalAmount = BigDecimal.ZERO;
        List<InvoiceDetail> details = new ArrayList<>();

        for (Attendance a : attendances) {
            Session s = a.getSession();
            // Chỉ tính session COMPLETED (hoặc SCHEDULED đã điểm danh)
            if (s.getStatus() == SessionStatus.CANCELLED) continue;

            boolean shouldCharge = false;
            switch (a.getStatus()) {
                case PRESENT:
                case ABSENT: // Mặc định vắng mặt không phép vẫn tính tiền
                    shouldCharge = true;
                    break;
                case EXCUSED:
                case CANCELLED:
                    shouldCharge = false;
                    break;
            }

            if (shouldCharge) {
                BigDecimal price = a.getPriceSnapshot() != null ? a.getPriceSnapshot() : BigDecimal.ZERO;
                totalAmount = totalAmount.add(price);

                InvoiceDetail detail = InvoiceDetail.builder()
                        .invoice(invoice)
                        .attendance(a)
                        .course(s.getGroup().getCourse())
                        .price(price)
                        .sessionDate(s.getDate())
                        .status(a.getStatus())
                        .build();
                details.add(detail);
            }
        }

        if (details.isEmpty()) {
            throw new BusinessException("Học sinh không có buổi học nào bị tính phí trong tháng này.");
        }

        invoice.setStudent(student);
        invoice.setMonth(month);
        invoice.setYear(year);
        invoice.setTotalSessions(details.size());
        invoice.setTotalAmount(totalAmount);
        
        // Cập nhật status dựa trên paidAmount
        if (invoice.getPaidAmount() == null) invoice.setPaidAmount(BigDecimal.ZERO);
        updateInvoiceStatus(invoice);

        invoice.setNeedRegenerate(false);
        invoice.setGeneratedBy(currentUser);
        invoice.setGeneratedAt(java.time.LocalDateTime.now());
        
        // Lưu invoice trước để có ID
        Invoice savedInvoice = invoiceRepository.save(invoice);
        
        // Lưu details
        details.forEach(d -> d.setInvoice(savedInvoice));
        invoiceDetailRepository.saveAll(details);
        savedInvoice.setDetails(details);

        return InvoiceResponse.fromEntity(savedInvoice);
    }

    /**
     * Đổi trạng thái hóa đơn thành FINALIZED
     */
    @Transactional
    @Auditable(action = "FINALIZE", entityType = "INVOICE")
    public InvoiceResponse finalize(Long invoiceId) {
        Invoice invoice = invoiceRepository.findById(invoiceId)
                .orElseThrow(() -> new BusinessException("Không tìm thấy hóa đơn"));
        invoice.setInvoiceStatus(InvoiceDocumentStatus.FINALIZED);
        return InvoiceResponse.fromEntity(invoiceRepository.save(invoice));
    }

    /**
     * Tạo hóa đơn cho tất cả học sinh của 1 giáo viên trong 1 tháng
     */
    @Transactional
    public List<InvoiceResponse> generateForTeacher(Long teacherId, Integer month, Integer year, User currentUser) {
        List<User> students = userRepository.findByRoleAndCreatedById(Role.STUDENT, teacherId);
        List<InvoiceResponse> responses = new ArrayList<>();
        for (User student : students) {
            try {
                responses.add(generateForStudent(student.getId(), month, year, currentUser));
            } catch (BusinessException e) {
                // Bỏ qua nếu không có buổi học nào hoặc đã tạo
                log.info("Bỏ qua học sinh {}: {}", student.getId(), e.getMessage());
            }
        }
        return responses;
    }

    @Transactional(readOnly = true)
    public InvoiceResponse getById(Long id, User currentUser) {
        Invoice invoice = invoiceRepository.findById(id)
                .orElseThrow(() -> new BusinessException("Không tìm thấy hóa đơn"));
        
        // Phân quyền
        if (currentUser.getRole() == Role.STUDENT && !invoice.getStudent().getId().equals(currentUser.getId())) {
            throw new BusinessException("Bạn không có quyền xem hóa đơn này");
        } else if (currentUser.getRole() == Role.TEACHER && !invoice.getStudent().getCreatedById().equals(currentUser.getId())) {
            throw new BusinessException("Bạn không có quyền xem hóa đơn này");
        }
        
        return InvoiceResponse.fromEntity(invoice);
    }

    @Transactional(readOnly = true)
    public List<InvoiceResponse> listByStudent(Long studentId) {
        return invoiceRepository.findByStudentIdOrderByYearDescMonthDesc(studentId).stream()
                .map(InvoiceResponse::fromEntity)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<InvoiceResponse> listAll(Integer month, Integer year, String status) {
        List<Invoice> invoices;
        if (month != null && year != null) {
            invoices = invoiceRepository.findByMonthAndYear(month, year);
        } else {
            invoices = invoiceRepository.findAll();
        }
        
        if (status != null && !status.isEmpty()) {
            InvoiceStatus s = InvoiceStatus.valueOf(status);
            invoices = invoices.stream().filter(i -> i.getStatus() == s).toList();
        }
        
        return invoices.stream().map(InvoiceResponse::fromEntity).toList();
    }
    
    @Transactional(readOnly = true)
    public List<InvoiceResponse> listByTeacher(Long teacherId, Integer month, Integer year, String status) {
        return invoiceRepository.findByTeacherIdAndMonthAndYear(teacherId, month, year).stream()
                .map(InvoiceResponse::fromEntity)
                .toList();
    }

    private void updateInvoiceStatus(Invoice invoice) {
        if (invoice.getPaidAmount().compareTo(BigDecimal.ZERO) == 0) {
            invoice.setStatus(InvoiceStatus.UNPAID);
        } else if (invoice.getPaidAmount().compareTo(invoice.getTotalAmount()) < 0) {
            invoice.setStatus(InvoiceStatus.PARTIAL);
        } else {
            invoice.setStatus(InvoiceStatus.PAID);
        }
    }
}
