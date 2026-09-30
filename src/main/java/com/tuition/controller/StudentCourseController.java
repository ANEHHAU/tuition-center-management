package com.tuition.controller;

import com.tuition.dto.CourseResponse;
import com.tuition.dto.PageResponse;
import com.tuition.dto.BaseSearchRequest;
import com.tuition.entity.Course;
import com.tuition.entity.Enrollment;
import com.tuition.entity.EnrollmentStatus;
import com.tuition.entity.User;
import com.tuition.repository.CourseRepository;
import com.tuition.repository.EnrollmentRepository;
import com.tuition.repository.UserRepository;
import com.tuition.service.CourseService;
import com.tuition.service.StudentService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/student/courses")
@RequiredArgsConstructor
@PreAuthorize("hasRole('STUDENT')")
public class StudentCourseController {

    private final StudentService studentService;
    private final CourseService courseService;
    private final UserRepository userRepository;
    private final EnrollmentRepository enrollmentRepository;

    private User getCurrentUser(UserDetails userDetails) {
        return userRepository.findByUsername(userDetails.getUsername()).orElseThrow();
    }

    @GetMapping
    public PageResponse<CourseResponse> getMyCourses(
            @ModelAttribute BaseSearchRequest request,
            @AuthenticationPrincipal UserDetails userDetails) {
        return studentService.getMyCourses(getCurrentUser(userDetails), request);
    }

    @GetMapping("/{id}")
    public Map<String, Object> getCourseDetail(
            @PathVariable Long id,
            @AuthenticationPrincipal UserDetails userDetails) {
        
        User student = getCurrentUser(userDetails);
        
        // Verify student is enrolled in this course
        List<Enrollment> enrollments = enrollmentRepository.findByStudentIdAndStatus(student.getId(), EnrollmentStatus.ACTIVE);
        boolean isEnrolled = enrollments.stream().anyMatch(e -> e.getGroup().getCourse().getId().equals(id));
        
        if (!isEnrolled) {
            throw new com.tuition.exception.BusinessException("Bạn chưa đăng ký khóa học này");
        }
        
        CourseResponse course = courseService.getById(id, student);
        
        // Find which groups the student is in for this course
        List<Map<String, Object>> groups = enrollments.stream()
                .filter(e -> e.getGroup().getCourse().getId().equals(id))
                .map(e -> Map.<String, Object>of(
                        "id", e.getGroup().getId(),
                        "name", e.getGroup().getName(),
                        "teacher", course.teacherName(),
                        "joinDate", e.getJoinDate()
                ))
                .toList();
                
        return Map.of(
                "course", course,
                "myGroups", groups
        );
    }
}
