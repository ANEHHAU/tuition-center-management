package com.tuition.controller;

import com.tuition.service.StudentService;
import com.tuition.entity.User;
import com.tuition.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/api/student/dashboard")
@RequiredArgsConstructor
@PreAuthorize("hasRole('STUDENT')")
public class StudentDashboardController {

    private final StudentService studentService;
    private final UserRepository userRepository;

    private User getCurrentUser(UserDetails userDetails) {
        return userRepository.findByUsername(userDetails.getUsername()).orElseThrow();
    }

    @GetMapping("/stats")
    public Map<String, Object> getStats(@AuthenticationPrincipal UserDetails userDetails) {
        return studentService.getDashboardStats(getCurrentUser(userDetails));
    }

    @GetMapping("/today-sessions")
    public Object getTodaySessions(@AuthenticationPrincipal UserDetails userDetails) {
        return studentService.getTodaySessions(getCurrentUser(userDetails));
    }

    @GetMapping("/upcoming-sessions")
    public Object getUpcomingSessions(
            @RequestParam(defaultValue = "7") int days,
            @AuthenticationPrincipal UserDetails userDetails) {
        return studentService.getUpcomingSessions(getCurrentUser(userDetails), days);
    }
}
