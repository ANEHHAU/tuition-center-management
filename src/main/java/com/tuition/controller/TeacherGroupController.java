package com.tuition.controller;

import com.tuition.dto.EnrollmentRequest;
import com.tuition.dto.EnrollmentResponse;
import com.tuition.dto.GroupRequest;
import com.tuition.dto.GroupResponse;
import com.tuition.entity.User;
import com.tuition.repository.UserRepository;
import com.tuition.service.GroupService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import com.tuition.dto.PageResponse;
import com.tuition.service.TeacherService;

@RestController
@RequestMapping("/api/teacher/groups")
@RequiredArgsConstructor
@PreAuthorize("hasRole('TEACHER')")
public class TeacherGroupController {

    private final GroupService groupService;
    private final TeacherService teacherService;
    private final UserRepository userRepository;

    private User getCurrentUser(UserDetails userDetails) {
        return userRepository.findByUsername(userDetails.getUsername()).orElseThrow();
    }

    @GetMapping
    public PageResponse<GroupResponse> getMyGroups(
            @ModelAttribute com.tuition.dto.BaseSearchRequest req,
            @RequestParam(required = false) Long courseId,
            @RequestParam(required = false) String status,
            @AuthenticationPrincipal UserDetails userDetails) {
        return teacherService.searchGroups(getCurrentUser(userDetails), req.getKeyword(), courseId, status, req.getPage(), req.getSize(), req.getSortBy(), req.getSortDir());
    }

    @GetMapping("/{id}")
    public GroupResponse getGroup(@PathVariable Long id, @AuthenticationPrincipal UserDetails userDetails) {
        return groupService.getById(id, getCurrentUser(userDetails));
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public GroupResponse createGroup(@RequestBody @Valid GroupRequest req, @AuthenticationPrincipal UserDetails userDetails) {
        return groupService.create(req, getCurrentUser(userDetails));
    }

    @PutMapping("/{id}")
    public GroupResponse updateGroup(@PathVariable Long id, @RequestBody @Valid GroupRequest req, @AuthenticationPrincipal UserDetails userDetails) {
        return groupService.update(id, req, getCurrentUser(userDetails));
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteGroup(@PathVariable Long id, @AuthenticationPrincipal UserDetails userDetails) {
        groupService.delete(id, getCurrentUser(userDetails));
    }

    @PostMapping("/{groupId}/students")
    public EnrollmentResponse addStudent(@PathVariable Long groupId, @RequestBody @Valid EnrollmentRequest req, @AuthenticationPrincipal UserDetails userDetails) {
        return groupService.addStudent(groupId, req.studentId(), req.joinDate(), getCurrentUser(userDetails));
    }

    @DeleteMapping("/{groupId}/students/{studentId}")
    public EnrollmentResponse removeStudent(@PathVariable Long groupId, @PathVariable Long studentId, @RequestParam(required = false) LocalDate leaveDate, @AuthenticationPrincipal UserDetails userDetails) {
        return groupService.removeStudent(groupId, studentId, leaveDate, getCurrentUser(userDetails));
    }

    @GetMapping("/{groupId}/students")
    public List<EnrollmentResponse> getStudentsInGroup(@PathVariable Long groupId, @AuthenticationPrincipal UserDetails userDetails) {
        return groupService.getStudentsInGroup(groupId, getCurrentUser(userDetails));
    }

    @PostMapping("/{groupId}/public-link/regenerate")
    public Map<String, String> regeneratePublicLink(@PathVariable Long groupId, @AuthenticationPrincipal UserDetails userDetails) {
        String token = groupService.regeneratePublicLink(groupId, getCurrentUser(userDetails));
        return Map.of("url", "/public/schedule/" + token, "token", token);
    }

    @PutMapping("/{groupId}/public-link/revoke")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void revokePublicLink(@PathVariable Long groupId, @AuthenticationPrincipal UserDetails userDetails) {
        groupService.revokePublicLink(groupId, getCurrentUser(userDetails));
    }

    @GetMapping("/{groupId}/public-link")
    public Map<String, String> getPublicLink(@PathVariable Long groupId, @AuthenticationPrincipal UserDetails userDetails) {
        String url = groupService.getPublicLink(groupId, getCurrentUser(userDetails));
        return Map.of("url", url);
    }
}
