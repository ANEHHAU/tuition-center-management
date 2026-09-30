package com.tuition.service;

import com.tuition.dto.AttendanceItemRequest;
import com.tuition.dto.AttendanceResponse;
import com.tuition.entity.*;
import com.tuition.exception.BusinessException;
import com.tuition.repository.AttendanceRepository;
import com.tuition.repository.EnrollmentRepository;
import com.tuition.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Service quản lý điểm danh (Attendance).
 * Chỉ HS có Enrollment active TẠI THỜI ĐIỂM session.date mới được điểm danh.
 */
@Service
@RequiredArgsConstructor
public class AttendanceService {

    private final AttendanceRepository attendanceRepository;
    private final EnrollmentRepository enrollmentRepository;
    private final SessionService sessionService;
    private final UserRepository userRepository;
    private final com.tuition.repository.InvoiceRepository invoiceRepository;

    /**
     * Lấy danh sách HS cần điểm danh cho 1 session.
     * Query enrollment active tại session.date (KHÔNG dùng ngày hiện tại).
     * Kèm theo trạng thái điểm danh hiện tại (nếu đã điểm).
     */
    @Transactional(readOnly = true)
    public AttendanceResponse getStudentsToAttend(Long sessionId) {
        Session session = sessionService.getSessionOrThrow(sessionId);
        LocalDate sessionDate = session.getDate();
        Long groupId = session.getGroup().getId();

        // Lấy enrollment active tại ngày session
        List<Enrollment> activeEnrollments = enrollmentRepository.findByGroupIdAndStatus(groupId, EnrollmentStatus.ACTIVE)
                .stream()
                .filter(e -> !e.getJoinDate().isAfter(sessionDate)
                        && (e.getLeaveDate() == null || !e.getLeaveDate().isBefore(sessionDate)))
                .toList();

        List<AttendanceResponse.AttendanceItem> items = new ArrayList<>();
        for (Enrollment enrollment : activeEnrollments) {
            User student = enrollment.getStudent();
            // Tìm attendance hiện tại (nếu đã điểm danh)
            var existing = attendanceRepository.findBySessionIdAndStudentId(sessionId, student.getId());
            items.add(new AttendanceResponse.AttendanceItem(
                    student.getId(),
                    student.getFullName(),
                    existing.map(Attendance::getStatus).orElse(null),
                    existing.map(Attendance::getNote).orElse(null),
                    existing.map(Attendance::getRecordedAt).orElse(null),
                    existing.map(Attendance::getPriceSnapshot).orElse(null)
            ));
        }

        return new AttendanceResponse(sessionId, items);
    }

    /**
     * Lưu/cập nhật điểm danh cho 1 session.
     * - Validate session thuộc teacher
     * - price_snapshot = course.price_per_session (chốt giá tại thời điểm điểm danh)
     * - Nếu đã có → update; chưa có → insert
     */
    @Transactional
    public AttendanceResponse saveAttendance(Long sessionId, List<AttendanceItemRequest> items, User currentUser) {
        Session session = sessionService.getSessionOrThrow(sessionId);
        Group group = session.getGroup();

        // Check quyền: session thuộc teacher hiện tại hoặc ADMIN
        if (currentUser.getRole() != Role.ADMIN
                && !group.getTeacher().getId().equals(currentUser.getId())) {
            throw new BusinessException("Bạn không có quyền điểm danh cho buổi học này");
        }

        // Lấy giá hiện tại của course để snapshot
        BigDecimal priceSnapshot = group.getCourse().getPricePerSession();

        List<AttendanceResponse.AttendanceItem> responseItems = new ArrayList<>();

        for (AttendanceItemRequest item : items) {
            User student = userRepository.findById(item.studentId())
                    .orElseThrow(() -> new BusinessException("Không tìm thấy học sinh với id: " + item.studentId()));

            // Tìm attendance đã có hoặc tạo mới
            Attendance attendance = attendanceRepository
                    .findBySessionIdAndStudentId(sessionId, item.studentId())
                    .orElse(Attendance.builder()
                            .session(session)
                            .student(student)
                            .priceSnapshot(priceSnapshot)
                            .build());

            attendance.setStatus(item.status());
            attendance.setNote(item.note());
            attendance.setRecordedBy(currentUser);
            attendance.setRecordedAt(LocalDateTime.now());

            // Chỉ set priceSnapshot cho record mới (chưa có id)
            if (attendance.getId() == null) {
                attendance.setPriceSnapshot(priceSnapshot);
            }

            Attendance saved = attendanceRepository.save(attendance);

            // Check invoice for this month
            invoiceRepository.findByStudentIdAndMonthAndYear(
                    student.getId(),
                    session.getDate().getMonthValue(),
                    session.getDate().getYear()
            ).ifPresent(invoice -> {
                if (invoice.getInvoiceStatus() == com.tuition.entity.InvoiceDocumentStatus.FINALIZED) {
                    throw new BusinessException("Hóa đơn của học sinh " + student.getFullName() + " trong tháng này đã chốt, không thể điểm danh lại.");
                }
                invoice.setNeedRegenerate(true);
                invoiceRepository.save(invoice);
            });

            responseItems.add(new AttendanceResponse.AttendanceItem(
                    student.getId(),
                    student.getFullName(),
                    saved.getStatus(),
                    saved.getNote(),
                    saved.getRecordedAt(),
                    saved.getPriceSnapshot()
            ));
        }

        return new AttendanceResponse(sessionId, responseItems);
    }

    /**
     * Lấy lịch sử điểm danh theo session
     */
    @Transactional(readOnly = true)
    public AttendanceResponse getBySession(Long sessionId) {
        List<Attendance> attendances = attendanceRepository.findBySessionId(sessionId);
        List<AttendanceResponse.AttendanceItem> items = attendances.stream()
                .map(a -> new AttendanceResponse.AttendanceItem(
                        a.getStudent().getId(),
                        a.getStudent().getFullName(),
                        a.getStatus(),
                        a.getNote(),
                        a.getRecordedAt(),
                        a.getPriceSnapshot()
                ))
                .toList();
        return new AttendanceResponse(sessionId, items);
    }

    /**
     * HS xem lịch sử điểm danh của mình trong khoảng thời gian
     */
    @Transactional(readOnly = true)
    public List<AttendanceResponse.AttendanceItem> getByStudent(Long studentId, LocalDate from, LocalDate to) {
        return attendanceRepository.findByStudentIdAndSessionDateBetween(studentId, from, to).stream()
                .map(a -> new AttendanceResponse.AttendanceItem(
                        a.getStudent().getId(),
                        a.getStudent().getFullName(),
                        a.getStatus(),
                        a.getNote(),
                        a.getRecordedAt(),
                        a.getPriceSnapshot()
                ))
                .toList();
    }
}
