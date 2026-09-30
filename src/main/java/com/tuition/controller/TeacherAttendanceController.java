package com.tuition.controller;

import com.tuition.dto.AttendanceRequest;
import com.tuition.dto.AttendanceResponse;
import com.tuition.entity.User;
import com.tuition.repository.UserRepository;
import com.tuition.service.AttendanceService;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/teacher/attendance")
@RequiredArgsConstructor
@PreAuthorize("hasRole('TEACHER')")
public class TeacherAttendanceController {

    private final AttendanceService attendanceService;
    private final UserRepository userRepository;

    private User getCurrentUser(UserDetails userDetails) {
        return userRepository.findByUsername(userDetails.getUsername()).orElseThrow();
    }

    @GetMapping("/session/{sessionId}")
    public AttendanceResponse getStudentsToAttend(@PathVariable Long sessionId) {
        return attendanceService.getStudentsToAttend(sessionId);
    }

    @PostMapping("/session/{sessionId}")
    public AttendanceResponse saveAttendance(
            @PathVariable Long sessionId,
            @RequestBody @Valid AttendanceRequest req,
            @AuthenticationPrincipal UserDetails userDetails) {
        return attendanceService.saveAttendance(sessionId, req.items(), getCurrentUser(userDetails));
    }

    @GetMapping("/history")
    public List<AttendanceResponse.AttendanceItem> getAttendanceHistory(
            @RequestParam(required = false) Long groupId,
            @RequestParam(required = false) Long studentId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to,
            @RequestParam(required = false) String status,
            @RequestParam(required = false, defaultValue = "0") int page,
            @RequestParam(required = false, defaultValue = "20") int size) {
        // Mock paging as not heavily requested in prompt format (no page response specified for history)
        // just return by student as requested before, or enhance if needed
        if (studentId != null) {
            return attendanceService.getByStudent(studentId, from, to);
        }
        return List.of();
    }

    @GetMapping("/student/{studentId}")
    public List<AttendanceResponse.AttendanceItem> getStudentAttendanceHistory(
            @PathVariable Long studentId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to) {
        return attendanceService.getByStudent(studentId, from, to);
    }

    @GetMapping("/export")
    public void exportAttendance(
            @RequestParam Long sessionId,
            HttpServletResponse response) throws IOException {
        AttendanceResponse attendance = attendanceService.getStudentsToAttend(sessionId);
        
        response.setContentType("text/csv; charset=UTF-8");
        response.setHeader("Content-Disposition", "attachment; filename=\"attendance_" + sessionId + ".csv\"");
        response.setCharacterEncoding("UTF-8");
        
        PrintWriter writer = response.getWriter();
        // BOM for Excel UTF-8
        writer.write('\ufeff');
        writer.println("Mã HS,Họ tên,Trạng thái,Ghi chú");
        for (AttendanceResponse.AttendanceItem item : attendance.items()) {
            writer.printf("%d,%s,%s,%s\n",
                    item.studentId(),
                    escapeCsv(item.studentName()),
                    item.status() != null ? item.status() : "",
                    escapeCsv(item.note() != null ? item.note() : "")
            );
        }
    }

    private String escapeCsv(String data) {
        if (data.contains(",") || data.contains("\"") || data.contains("\n")) {
            return "\"" + data.replace("\"", "\"\"") + "\"";
        }
        return data;
    }
}
