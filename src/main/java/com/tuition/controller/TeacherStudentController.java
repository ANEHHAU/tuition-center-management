package com.tuition.controller;

import com.tuition.dto.RegisterRequest;
import com.tuition.dto.UserResponse;
import com.tuition.entity.Role;
import com.tuition.entity.User;
import com.tuition.exception.BusinessException;
import com.tuition.repository.UserRepository;
import com.tuition.service.AuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/teacher/students")
@RequiredArgsConstructor
public class TeacherStudentController {

    private final UserRepository userRepository;
    private final AuthService authService;

    private User getCurrentUser(UserDetails userDetails) {
        return userRepository.findByUsername(userDetails.getUsername()).orElseThrow();
    }

    @GetMapping
    public List<UserResponse> getMyStudents(@AuthenticationPrincipal UserDetails userDetails) {
        User currentUser = getCurrentUser(userDetails);
        return userRepository.findByRoleAndCreatedById(Role.STUDENT, currentUser.getId()).stream()
                .map(UserResponse::fromEntity)
                .toList();
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public UserResponse createStudent(@RequestBody @Valid RegisterRequest req, @AuthenticationPrincipal UserDetails userDetails) {
        if (req.role() != Role.STUDENT) {
            throw new BusinessException("Giáo viên chỉ có thể tạo tài khoản cho HỌC SINH (role = STUDENT)");
        }
        User currentUser = getCurrentUser(userDetails);
        UserResponse response = authService.createByAdmin(req);
        
        // Update createdById manually since AuthService doesn't know about it
        User student = userRepository.findById(response.id()).orElseThrow();
        student.setCreatedById(currentUser.getId());
        userRepository.save(student);
        
        return UserResponse.fromEntity(student);
    }
}
