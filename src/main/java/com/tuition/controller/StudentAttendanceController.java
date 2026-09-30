package com.tuition.controller;

import com.tuition.dto.StudentAttendanceResponse;
import com.tuition.dto.PageResponse;
import com.tuition.dto.BaseSearchRequest;
import com.tuition.entity.AttendanceStatus;
import com.tuition.entity.User;
import com.tuition.repository.UserRepository;
import com.tuition.service.StudentService;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.Map;

@RestController
@RequestMapping("/api/student/attendance")
@RequiredArgsConstructor
@PreAuthorize("hasRole('STUDENT')")
public class StudentAttendanceController {

    private final StudentService studentService;
    private final UserRepository userRepository;

    private User getCurrentUser(UserDetails userDetails) {
        return userRepository.findByUsername(userDetails.getUsername()).orElseThrow();
    }

    @GetMapping
    public PageResponse<StudentAttendanceResponse> getAttendance(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to,
            @RequestParam(required = false) Long courseId,
            @RequestParam(required = false) AttendanceStatus status,
            @ModelAttribute BaseSearchRequest request,
            @AuthenticationPrincipal UserDetails userDetails) {
        
        LocalDate queryFrom = from != null ? from : LocalDate.now().minusMonths(3);
        LocalDate queryTo = to != null ? to : LocalDate.now().plusMonths(1);
        
        return studentService.getAttendanceHistory(getCurrentUser(userDetails), queryFrom, queryTo, courseId, status, request);
    }

    @GetMapping("/stats")
    public Map<String, Object> getStats(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to,
            @AuthenticationPrincipal UserDetails userDetails) {
        
        LocalDate queryFrom = from != null ? from : LocalDate.now().minusMonths(3);
        LocalDate queryTo = to != null ? to : LocalDate.now().plusMonths(1);
        
        return studentService.getAttendanceStats(getCurrentUser(userDetails), queryFrom, queryTo);
    }
}
