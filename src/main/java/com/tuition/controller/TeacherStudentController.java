package com.tuition.controller;

import com.tuition.dto.BaseSearchRequest;
import com.tuition.dto.PageResponse;
import com.tuition.dto.RegisterRequest;
import com.tuition.dto.UpdateUserRequest;
import com.tuition.dto.UserResponse;
import com.tuition.entity.User;
import com.tuition.repository.UserRepository;
import com.tuition.service.TeacherService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/teacher/students")
@RequiredArgsConstructor
@PreAuthorize("hasRole('TEACHER')")
public class TeacherStudentController {

    private final TeacherService teacherService;
    private final UserRepository userRepository;

    private User getCurrentUser(UserDetails userDetails) {
        return userRepository.findByUsername(userDetails.getUsername()).orElseThrow();
    }

    @GetMapping
    public PageResponse<com.tuition.dto.StudentWithGroupsResponse> getStudents(
            @ModelAttribute BaseSearchRequest req,
            @RequestParam(required = false) String status,
            @AuthenticationPrincipal UserDetails userDetails) {
        return teacherService.searchStudentsWithGroups(getCurrentUser(userDetails), req);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public UserResponse createStudent(
            @RequestBody @Valid RegisterRequest req,
            @AuthenticationPrincipal UserDetails userDetails) {
        return teacherService.createStudent(getCurrentUser(userDetails), req);
    }

    @PutMapping("/{id}")
    public UserResponse updateStudent(
            @PathVariable Long id,
            @RequestBody @Valid UpdateUserRequest req,
            @AuthenticationPrincipal UserDetails userDetails) {
        return teacherService.updateStudent(getCurrentUser(userDetails), id, req);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteStudent(
            @PathVariable Long id,
            @AuthenticationPrincipal UserDetails userDetails) {
        teacherService.softDeleteStudent(getCurrentUser(userDetails), id);
    }

    @PostMapping("/{id}/restore")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void restoreStudent(
            @PathVariable Long id,
            @AuthenticationPrincipal UserDetails userDetails) {
        teacherService.restoreStudent(getCurrentUser(userDetails), id);
    }

    @PutMapping("/{id}/reset-password")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void resetPassword(
            @PathVariable Long id,
            @RequestBody Map<String, String> payload,
            @AuthenticationPrincipal UserDetails userDetails) {
        teacherService.resetStudentPassword(getCurrentUser(userDetails), id, payload.get("newPassword"));
    }

    @GetMapping("/{id}/detail")
    public UserResponse getStudentDetail(
            @PathVariable Long id,
            @AuthenticationPrincipal UserDetails userDetails) {
        return teacherService.getStudentDetail(getCurrentUser(userDetails), id);
    }

    @GetMapping("/find")
    public UserResponse findStudentByEmailOrPhone(
            @RequestParam String query,
            @AuthenticationPrincipal UserDetails userDetails) {
        return teacherService.findStudentByEmailOrPhone(query);
    }

    @PostMapping("/enroll/{studentId}/group/{groupId}")
    public void enrollStudentToGroup(
            @PathVariable Long studentId,
            @PathVariable Long groupId,
            @AuthenticationPrincipal UserDetails userDetails) {
        teacherService.enrollStudentToGroup(getCurrentUser(userDetails), studentId, groupId);
    }
}
