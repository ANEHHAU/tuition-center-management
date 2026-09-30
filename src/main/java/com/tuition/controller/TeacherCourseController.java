package com.tuition.controller;

import com.tuition.dto.BaseSearchRequest;
import com.tuition.dto.CourseRequest;
import com.tuition.dto.CourseResponse;
import com.tuition.dto.PageResponse;
import com.tuition.entity.User;
import com.tuition.repository.UserRepository;
import com.tuition.service.CourseService;
import com.tuition.service.TeacherService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/teacher/courses")
@RequiredArgsConstructor
@PreAuthorize("hasRole('TEACHER')")
public class TeacherCourseController {

    private final CourseService courseService;
    private final TeacherService teacherService;
    private final UserRepository userRepository;

    private User getCurrentUser(UserDetails userDetails) {
        return userRepository.findByUsername(userDetails.getUsername()).orElseThrow();
    }

    @GetMapping
    public PageResponse<CourseResponse> getMyCourses(
            @ModelAttribute BaseSearchRequest req,
            @RequestParam(required = false) String status,
            @AuthenticationPrincipal UserDetails userDetails) {
        return teacherService.searchCourses(getCurrentUser(userDetails), req.getKeyword(), status, req.getPage(), req.getSize(), req.getSortBy(), req.getSortDir());
    }

    @GetMapping("/{id}")
    @PreAuthorize("@courseSecurityService.isOwner(#id, authentication)")
    public CourseResponse getCourse(@PathVariable Long id, @AuthenticationPrincipal UserDetails userDetails) {
        return courseService.getById(id, getCurrentUser(userDetails));
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public CourseResponse createCourse(@RequestBody @Valid CourseRequest req, @AuthenticationPrincipal UserDetails userDetails) {
        return courseService.create(req, getCurrentUser(userDetails));
    }

    @PutMapping("/{id}")
    @PreAuthorize("@courseSecurityService.isOwner(#id, authentication)")
    public CourseResponse updateCourse(@PathVariable Long id, @RequestBody @Valid CourseRequest req, @AuthenticationPrincipal UserDetails userDetails) {
        return courseService.update(id, req, getCurrentUser(userDetails));
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @PreAuthorize("@courseSecurityService.isOwner(#id, authentication)")
    public void deleteCourse(@PathVariable Long id, @AuthenticationPrincipal UserDetails userDetails) {
        courseService.delete(id, getCurrentUser(userDetails));
    }
}
