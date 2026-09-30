package com.tuition.controller;

import com.tuition.dto.SessionResponse;
import com.tuition.entity.User;
import com.tuition.repository.UserRepository;
import com.tuition.service.TeacherService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/teacher/dashboard")
@RequiredArgsConstructor
@PreAuthorize("hasRole('TEACHER')")
public class TeacherDashboardController {

    private final TeacherService teacherService;
    private final UserRepository userRepository;

    private User getCurrentUser(UserDetails userDetails) {
        return userRepository.findByUsername(userDetails.getUsername()).orElseThrow();
    }

    @GetMapping("/stats")
    public Map<String, Object> getStats(@AuthenticationPrincipal UserDetails userDetails) {
        return teacherService.getDashboardStats(getCurrentUser(userDetails));
    }

    @GetMapping("/today-sessions")
    public List<SessionResponse> getTodaySessions(@AuthenticationPrincipal UserDetails userDetails) {
        return teacherService.getTodaySessions(getCurrentUser(userDetails));
    }

    @GetMapping("/recent-payments")
    public List<Map<String, Object>> getRecentPayments(@AuthenticationPrincipal UserDetails userDetails) {
        return teacherService.getRecentPayments(getCurrentUser(userDetails));
    }
}
