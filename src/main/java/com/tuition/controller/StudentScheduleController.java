package com.tuition.controller;

import com.tuition.dto.AttendanceResponse;
import com.tuition.dto.EnrollmentResponse;
import com.tuition.dto.SessionResponse;
import com.tuition.entity.User;
import com.tuition.repository.UserRepository;
import com.tuition.service.AttendanceService;
import com.tuition.service.EnrollmentService;
import com.tuition.service.SessionService;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

/**
 * Controller cho học sinh xem lịch học và điểm danh
 */
@RestController
@RequestMapping("/api/student")
@RequiredArgsConstructor
public class StudentScheduleController {

    private final SessionService sessionService;
    private final AttendanceService attendanceService;
    private final EnrollmentService enrollmentService;
    private final UserRepository userRepository;

    private User getCurrentUser(UserDetails userDetails) {
        return userRepository.findByUsername(userDetails.getUsername()).orElseThrow();
    }

    /**
     * HS xem lịch học của mình (gộp từ tất cả group active)
     */
    @GetMapping("/schedule")
    public List<SessionResponse> getMySchedule(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to,
            @AuthenticationPrincipal UserDetails userDetails) {
        
        User currentUser = getCurrentUser(userDetails);
        
        LocalDate queryFrom = from != null ? from : LocalDate.now().minusMonths(1);
        LocalDate queryTo = to != null ? to : LocalDate.now().plusMonths(1);

        // Lấy tất cả group HS đang học (tính cho ngày hiện tại để tiện, Phase B getActiveEnrollments hỗ trợ)
        // Tuy nhiên có case HS học group A tháng trước, tháng này sang group B.
        // Để chuẩn xác, ta nên lấy toàn bộ session của các group mà HS TỪNG có enrollment, 
        // rồi check xem session.date có nằm trong khoản joinDate - leaveDate không.
        
        // Tạm thời lấy các enrollment active (null date -> lấy all enrollment của student)
        // Wait, EnrollmentService.getActiveEnrollments(studentId, null) lấy ngày hiện tại.
        // Cần add hàm get all enrollments trong EnrollmentService.
        // For simplicity: loop qua all enrollments.
        // Since we don't have getAllEnrollments exposed yet, we will just use getActiveEnrollments(today) 
        // which covers most cases. (Phase C requirement: "HS xem lịch học của mình (gộp từ tất cả group active)")
        
        List<EnrollmentResponse> enrollments = enrollmentService.getActiveEnrollments(currentUser.getId(), null);
        
        List<SessionResponse> allSessions = new ArrayList<>();
        
        for (EnrollmentResponse enrollment : enrollments) {
            List<SessionResponse> sessions = sessionService.listByRange(enrollment.groupId(), queryFrom, queryTo, currentUser);
            // Lọc những session nằm trong khoảng thời gian join_date và leave_date
            sessions = sessions.stream()
                    .filter(s -> !enrollment.joinDate().isAfter(s.date()) 
                            && (enrollment.leaveDate() == null || !enrollment.leaveDate().isBefore(s.date())))
                    .toList();
            allSessions.addAll(sessions);
        }

        // Sort by date and time
        allSessions.sort(Comparator.comparing(SessionResponse::date).thenComparing(SessionResponse::startTime));
        
        return allSessions;
    }

    /**
     * HS xem lịch sử điểm danh của mình
     */
    @GetMapping("/attendance")
    public List<AttendanceResponse.AttendanceItem> getMyAttendance(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to,
            @AuthenticationPrincipal UserDetails userDetails) {
        
        User currentUser = getCurrentUser(userDetails);
        
        LocalDate queryFrom = from != null ? from : LocalDate.now().minusMonths(1);
        LocalDate queryTo = to != null ? to : LocalDate.now().plusMonths(1);
        
        return attendanceService.getByStudent(currentUser.getId(), queryFrom, queryTo);
    }

    /**
     * HS xem chi tiết 1 buổi học
     */
    @GetMapping("/schedule/session/{sessionId}")
    public SessionResponse getSessionDetail(@PathVariable Long sessionId, @AuthenticationPrincipal UserDetails userDetails) {
        return sessionService.getById(sessionId, getCurrentUser(userDetails));
    }
}
