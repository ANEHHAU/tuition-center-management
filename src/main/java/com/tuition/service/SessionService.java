package com.tuition.service;

import com.tuition.dto.SessionRequest;
import com.tuition.dto.SessionResponse;
import com.tuition.entity.*;
import com.tuition.exception.BusinessException;
import com.tuition.repository.AttendanceRepository;
import com.tuition.repository.SessionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

/**
 * Service quản lý buổi học (Session).
 */
@Service
@RequiredArgsConstructor
public class SessionService {

    private final SessionRepository sessionRepository;
    private final AttendanceRepository attendanceRepository;
    private final GroupService groupService;
    private final com.tuition.repository.InvoiceRepository invoiceRepository;

    /**
     * Tạo buổi học mới.
     * - Validate group thuộc teacher
     * - Validate không trùng (group, date, startTime)
     */
    @Transactional
    public SessionResponse create(SessionRequest req, User currentUser) {
        Group group = groupService.getGroupOrThrow(req.groupId());
        checkGroupOwnership(group, currentUser);

        // Validate ngày học >= ngày bắt đầu group (nếu có)
        if (group.getStartDate() != null && req.date().isBefore(group.getStartDate())) {
            throw new BusinessException("Ngày học không được trước ngày bắt đầu của nhóm (" + group.getStartDate() + ")");
        }

        // Validate không trùng (group, date, startTime)
        if (sessionRepository.existsByGroupIdAndDateAndStartTime(req.groupId(), req.date(), req.startTime())) {
            throw new BusinessException("Đã tồn tại buổi học cùng ngày và giờ bắt đầu trong nhóm này");
        }

        // Validate giờ kết thúc > giờ bắt đầu
        if (!req.endTime().isAfter(req.startTime())) {
            throw new BusinessException("Giờ kết thúc phải sau giờ bắt đầu");
        }

        Session session = Session.builder()
                .group(group)
                .date(req.date())
                .startTime(req.startTime())
                .endTime(req.endTime())
                .room(req.room())
                .status(req.status() != null ? req.status() : SessionStatus.SCHEDULED)
                .note(req.note())
                .build();

        Session saved = sessionRepository.save(session);
        return SessionResponse.fromEntity(saved, 0);
    }

    /**
     * Cập nhật buổi học. Check quyền group.
     */
    @Transactional
    public SessionResponse update(Long id, SessionRequest req, User currentUser) {
        Session session = getSessionOrThrow(id);
        checkGroupOwnership(session.getGroup(), currentUser);

        session.setDate(req.date());
        session.setStartTime(req.startTime());
        session.setEndTime(req.endTime());
        session.setRoom(req.room());
        if (req.status() != null) session.setStatus(req.status());
        if (req.note() != null) session.setNote(req.note());

        Session saved = sessionRepository.save(session);
        long count = attendanceRepository.countBySessionId(id);
        return SessionResponse.fromEntity(saved, count);
    }

    /**
     * Hủy buổi học: set status = CANCELLED, tất cả attendance → CANCELLED
     */
    @Transactional
    public SessionResponse cancel(Long id, User currentUser) {
        Session session = getSessionOrThrow(id);
        checkGroupOwnership(session.getGroup(), currentUser);

        session.setStatus(SessionStatus.CANCELLED);
        sessionRepository.save(session);

        // Chuyển tất cả attendance của session này sang CANCELLED
        List<com.tuition.entity.Attendance> attendances = attendanceRepository.findBySessionId(id);
        for (com.tuition.entity.Attendance a : attendances) {
            a.setStatus(AttendanceStatus.CANCELLED);
            invoiceRepository.findByStudentIdAndMonthAndYear(
                    a.getStudent().getId(),
                    session.getDate().getMonthValue(),
                    session.getDate().getYear()
            ).ifPresent(invoice -> {
                if (invoice.getInvoiceStatus() == com.tuition.entity.InvoiceDocumentStatus.FINALIZED) {
                    throw new BusinessException("Hóa đơn của học sinh " + a.getStudent().getFullName() + " trong tháng này đã chốt, không thể hủy buổi học.");
                }
                invoice.setNeedRegenerate(true);
                invoiceRepository.save(invoice);
            });
        }
        attendanceRepository.saveAll(attendances);

        return SessionResponse.fromEntity(session, attendances.size());
    }

    /**
     * Xóa buổi học: chỉ khi chưa có attendance
     */
    @Transactional
    public void delete(Long id, User currentUser) {
        Session session = getSessionOrThrow(id);
        checkGroupOwnership(session.getGroup(), currentUser);

        long count = attendanceRepository.countBySessionId(id);
        if (count > 0) {
            throw new BusinessException("Không thể xóa buổi học đã có dữ liệu điểm danh. Hãy hủy (cancel) thay vì xóa.");
        }
        sessionRepository.delete(session);
    }

    /**
     * Lấy chi tiết buổi học
     */
    @Transactional(readOnly = true)
    public SessionResponse getById(Long id, User currentUser) {
        Session session = getSessionOrThrow(id);
        if (currentUser.getRole() != Role.ADMIN && currentUser.getRole() != Role.STUDENT) {
            checkGroupOwnership(session.getGroup(), currentUser);
        }
        long count = attendanceRepository.countBySessionId(id);
        return SessionResponse.fromEntity(session, count);
    }

    /**
     * Liệt kê sessions theo group
     */
    @Transactional(readOnly = true)
    public List<SessionResponse> listByGroup(Long groupId, User currentUser) {
        Group group = groupService.getGroupOrThrow(groupId);
        if (currentUser.getRole() != Role.ADMIN) {
            checkGroupOwnership(group, currentUser);
        }
        return sessionRepository.findByGroupIdOrderByDateAsc(groupId).stream()
                .map(s -> SessionResponse.fromEntity(s, attendanceRepository.countBySessionId(s.getId())))
                .toList();
    }

    /**
     * Liệt kê sessions trong khoảng thời gian
     */
    @Transactional(readOnly = true)
    public List<SessionResponse> listByRange(Long groupId, LocalDate from, LocalDate to, User currentUser) {
        Group group = groupService.getGroupOrThrow(groupId);
        if (currentUser.getRole() != Role.ADMIN) {
            checkGroupOwnership(group, currentUser);
        }
        return sessionRepository.findSessionsInRange(groupId, from, to).stream()
                .map(s -> SessionResponse.fromEntity(s, attendanceRepository.countBySessionId(s.getId())))
                .toList();
    }

    // ========= Helper =========

    public Session getSessionOrThrow(Long id) {
        return sessionRepository.findById(id)
                .orElseThrow(() -> new BusinessException("Không tìm thấy buổi học với id: " + id));
    }

    private void checkGroupOwnership(Group group, User currentUser) {
        if (currentUser.getRole() == Role.ADMIN) return;
        if (!group.getTeacher().getId().equals(currentUser.getId())) {
            throw new BusinessException("Bạn không có quyền thao tác trên nhóm này");
        }
    }
}
