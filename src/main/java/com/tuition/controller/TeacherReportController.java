package com.tuition.controller;

import com.tuition.entity.User;
import com.tuition.repository.UserRepository;
import com.tuition.service.TeacherService;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/teacher/reports")
@RequiredArgsConstructor
@PreAuthorize("hasRole('TEACHER')")
public class TeacherReportController {

    private final TeacherService teacherService;
    private final UserRepository userRepository;

    private User getCurrentUser(UserDetails userDetails) {
        return userRepository.findByUsername(userDetails.getUsername()).orElseThrow();
    }

    @GetMapping("/revenue")
    public Map<String, Object> getRevenue(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime from,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime to,
            @RequestParam(required = false) String groupBy,
            @AuthenticationPrincipal UserDetails userDetails) {
        if (from == null) from = LocalDateTime.now().minusMonths(1);
        if (to == null) to = LocalDateTime.now();
        return teacherService.getRevenueReport(getCurrentUser(userDetails), from, to);
    }

    @GetMapping("/revenue-by-course")
    public List<Map<String, Object>> getRevenueByCourse(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime from,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime to,
            @AuthenticationPrincipal UserDetails userDetails) {
        if (from == null) from = LocalDateTime.now().minusMonths(1);
        if (to == null) to = LocalDateTime.now();
        return teacherService.getRevenueByCourse(getCurrentUser(userDetails), from, to);
    }

    @GetMapping("/attendance-rate")
    public List<Map<String, Object>> getAttendanceRate(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to,
            @AuthenticationPrincipal UserDetails userDetails) {
        if (from == null) from = LocalDate.now().minusMonths(1);
        if (to == null) to = LocalDate.now();
        return teacherService.getAttendanceRate(getCurrentUser(userDetails), from, to);
    }

    @GetMapping("/top-students")
    public List<Map<String, Object>> getTopStudents(
            @RequestParam(required = false, defaultValue = "10") int limit,
            @AuthenticationPrincipal UserDetails userDetails) {
        return teacherService.getTopStudents(getCurrentUser(userDetails), limit);
    }
}
