package com.tuition.controller;

import com.tuition.dto.AttendanceRequest;
import com.tuition.dto.AttendanceResponse;
import com.tuition.entity.User;
import com.tuition.repository.UserRepository;
import com.tuition.service.AttendanceService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

/**
 * Controller quản lý điểm danh cho Teacher
 */
@RestController
@RequestMapping("/api/teacher/attendance")
@RequiredArgsConstructor
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

    @GetMapping("/student/{studentId}")
    public List<AttendanceResponse.AttendanceItem> getStudentAttendanceHistory(
            @PathVariable Long studentId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to) {
        return attendanceService.getByStudent(studentId, from, to);
    }
}
