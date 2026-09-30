package com.tuition.service;

import com.tuition.dto.*;
import com.tuition.entity.*;
import com.tuition.exception.BusinessException;
import com.tuition.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class StudentService {

    private final CourseRepository courseRepository;
    private final GroupRepository groupRepository;
    private final EnrollmentRepository enrollmentRepository;
    private final SessionRepository sessionRepository;
    private final AttendanceRepository attendanceRepository;
    private final InvoiceRepository invoiceRepository;
    private final PaymentRepository paymentRepository;

    @Transactional(readOnly = true)
    public Map<String, Object> getDashboardStats(User student) {
        Long studentId = student.getId();
        
        List<Enrollment> enrollments = enrollmentRepository.findByStudentIdAndStatus(studentId, EnrollmentStatus.ACTIVE);
        long totalGroups = enrollments.size();
        long totalCourses = enrollments.stream().map(e -> e.getGroup().getCourse().getId()).distinct().count();
        
        long totalSessionsAttended = attendanceRepository.countByStudentIdAndStatusAndSessionDateBetween(
                studentId, AttendanceStatus.PRESENT, LocalDate.of(2000, 1, 1), LocalDate.of(2100, 1, 1)
        );

        List<Invoice> invoices = invoiceRepository.findByStudentIdOrderByYearDescMonthDesc(studentId);
        BigDecimal currentDebt = invoices.stream()
                .filter(i -> i.getStatus() == InvoiceStatus.UNPAID || i.getStatus() == InvoiceStatus.PARTIAL)
                .map(i -> i.getTotalAmount().subtract(i.getPaidAmount()))
                .reduce(BigDecimal.ZERO, BigDecimal::add);
                
        long unreadInvoices = invoices.stream()
                .filter(i -> i.getStatus() == InvoiceStatus.UNPAID) // Simple heuristic
                .count();

        return Map.of(
                "totalCourses", totalCourses,
                "totalGroups", totalGroups,
                "totalSessionsAttended", totalSessionsAttended,
                "currentDebt", currentDebt,
                "unreadInvoices", unreadInvoices
        );
    }

    @Transactional(readOnly = true)
    public List<StudentSessionResponse> getUpcomingSessions(User student, int days) {
        LocalDate today = LocalDate.now();
        LocalDate to = today.plusDays(days);
        return getSessionsByRange(student, today, to);
    }
    
    @Transactional(readOnly = true)
    public List<StudentSessionResponse> getTodaySessions(User student) {
        LocalDate today = LocalDate.now();
        return getSessionsByRange(student, today, today);
    }

    @Transactional(readOnly = true)
    public List<StudentSessionResponse> getSessionsByRange(User student, LocalDate from, LocalDate to) {
        List<Enrollment> enrollments = enrollmentRepository.findByStudentIdAndStatus(student.getId(), EnrollmentStatus.ACTIVE);
        List<Session> allSessions = new ArrayList<>();
        
        for (Enrollment e : enrollments) {
            List<Session> sessions = sessionRepository.findByGroupIdAndDateBetween(e.getGroup().getId(), from, to);
            allSessions.addAll(sessions);
        }
        
        allSessions.sort(Comparator.comparing(Session::getDate).thenComparing(Session::getStartTime));
        
        return allSessions.stream().map(StudentSessionResponse::fromEntity).toList();
    }

    @Transactional(readOnly = true)
    public StudentSessionResponse getSessionDetail(User student, Long sessionId) {
        Session session = sessionRepository.findById(sessionId)
                .orElseThrow(() -> new BusinessException("Không tìm thấy buổi học"));
        
        StudentSessionResponse resp = StudentSessionResponse.fromEntity(session);
        
        // Attach student's attendance
        attendanceRepository.findBySessionIdAndStudentId(sessionId, student.getId()).ifPresent(att -> {
            resp.setMyAttendanceStatus(att.getStatus());
            resp.setMyAttendanceNote(att.getNote());
        });
        
        return resp;
    }

    @Transactional(readOnly = true)
    public PageResponse<StudentSessionResponse> searchSchedule(User student, LocalDate from, LocalDate to, Long courseId, BaseSearchRequest request) {
        List<StudentSessionResponse> all = getSessionsByRange(student, 
                from != null ? from : LocalDate.now().minusMonths(1), 
                to != null ? to : LocalDate.now().plusMonths(1));
                
        if (courseId != null) {
            all = all.stream().filter(s -> Objects.equals(s.getCourseId(), courseId)).toList();
        }
        
        // Paginate manually
        int start = request.getPage() * request.getSize();
        int end = Math.min((start + request.getSize()), all.size());
        List<StudentSessionResponse> pageContent = start <= end && start < all.size() ? all.subList(start, end) : Collections.emptyList();
        
        return PageResponse.of(new PageImpl<>(pageContent, PageRequest.of(request.getPage(), request.getSize()), all.size()));
    }

    @Transactional(readOnly = true)
    public PageResponse<CourseResponse> getMyCourses(User student, BaseSearchRequest request) {
        List<Enrollment> enrollments = enrollmentRepository.findByStudentIdAndStatus(student.getId(), EnrollmentStatus.ACTIVE);
        List<Course> courses = enrollments.stream().map(e -> e.getGroup().getCourse()).distinct().toList();
        
        int start = request.getPage() * request.getSize();
        int end = Math.min((start + request.getSize()), courses.size());
        List<Course> pageContent = start <= end && start < courses.size() ? courses.subList(start, end) : Collections.emptyList();
        
        List<CourseResponse> responses = pageContent.stream()
                .map(c -> CourseResponse.fromEntity(c, groupRepository.countByCourseId(c.getId())))
                .toList();
                
        return PageResponse.of(new PageImpl<>(responses, PageRequest.of(request.getPage(), request.getSize()), courses.size()));
    }

    @Transactional(readOnly = true)
    public Map<String, Object> getAttendanceStats(User student, LocalDate from, LocalDate to) {
        List<Enrollment> enrollments = enrollmentRepository.findByStudentIdAndStatus(student.getId(), EnrollmentStatus.ACTIVE);
        long present = 0, absent = 0, excused = 0, total = 0;
        
        for (Enrollment e : enrollments) {
            List<Session> sessions = sessionRepository.findByGroupIdAndDateBetween(e.getGroup().getId(), from, to);
            for (Session s : sessions) {
                Optional<Attendance> att = attendanceRepository.findBySessionIdAndStudentId(s.getId(), student.getId());
                if (att.isPresent()) {
                    total++;
                    switch (att.get().getStatus()) {
                        case PRESENT -> present++;
                        case ABSENT -> absent++;
                        case EXCUSED -> excused++;
                    }
                }
            }
        }
        
        double rate = total == 0 ? 0 : (double) present / total * 100;
        return Map.of(
                "present", present,
                "absent", absent,
                "excused", excused,
                "total", total,
                "rate", Math.round(rate * 10.0) / 10.0
        );
    }
    
    @Transactional(readOnly = true)
    public PageResponse<StudentAttendanceResponse> getAttendanceHistory(User student, LocalDate from, LocalDate to, Long courseId, AttendanceStatus status, BaseSearchRequest request) {
        List<Enrollment> enrollments = enrollmentRepository.findByStudentIdAndStatus(student.getId(), EnrollmentStatus.ACTIVE);
        List<StudentAttendanceResponse> items = new ArrayList<>();
        
        for (Enrollment e : enrollments) {
            if (courseId != null && !e.getGroup().getCourse().getId().equals(courseId)) continue;
            
            List<Session> sessions = sessionRepository.findByGroupIdAndDateBetween(e.getGroup().getId(), from, to);
            for (Session s : sessions) {
                Optional<Attendance> att = attendanceRepository.findBySessionIdAndStudentId(s.getId(), student.getId());
                if (att.isPresent()) {
                    if (status == null || att.get().getStatus() == status) {
                        items.add(StudentAttendanceResponse.fromEntity(att.get()));
                    }
                }
            }
        }
        
        items.sort(Comparator.comparing(StudentAttendanceResponse::getSessionDate).reversed());
        
        int start = request.getPage() * request.getSize();
        int end = Math.min((start + request.getSize()), items.size());
        List<StudentAttendanceResponse> pageContent = start <= end && start < items.size() ? items.subList(start, end) : Collections.emptyList();
        
        return PageResponse.of(new PageImpl<>(pageContent, PageRequest.of(request.getPage(), request.getSize()), items.size()));
    }
    
    @Transactional
    public void notifyPayment(User student, Long invoiceId, Map<String, Object> payload) {
        Invoice invoice = invoiceRepository.findById(invoiceId)
                .orElseThrow(() -> new BusinessException("Không tìm thấy hóa đơn"));
                
        if (!invoice.getStudent().getId().equals(student.getId())) {
            throw new BusinessException("Hóa đơn không thuộc về bạn");
        }
        
        BigDecimal amount = new BigDecimal(payload.get("amount").toString());
        String methodStr = payload.get("method").toString();
        String note = payload.getOrDefault("note", "").toString();
        
        PaymentMethod method;
        try {
            method = PaymentMethod.valueOf(methodStr);
        } catch (Exception e) {
            method = PaymentMethod.BANK_TRANSFER;
        }
        
        // Add note indicating it's reported by student
        String finalNote = "[HS BÁO CK] " + note;
        
        Payment payment = Payment.builder()
                .invoice(invoice)
                .amount(amount)
                .method(method)
                .paidAt(java.time.LocalDateTime.now())
                .recordedBy(student) // Student recorded it
                .note(finalNote)
                .build();
                
        paymentRepository.save(payment);
        
        // Update invoice
        BigDecimal newPaidAmount = invoice.getPaidAmount().add(amount);
        invoice.setPaidAmount(newPaidAmount);
        
        if (newPaidAmount.compareTo(invoice.getTotalAmount()) >= 0) {
            invoice.setStatus(InvoiceStatus.PAID);
        } else if (newPaidAmount.compareTo(BigDecimal.ZERO) > 0) {
            invoice.setStatus(InvoiceStatus.PARTIAL);
        }
        
        invoiceRepository.save(invoice);
    }
}
