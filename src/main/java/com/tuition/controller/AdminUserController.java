package com.tuition.controller;

import com.tuition.dto.RegisterRequest;
import com.tuition.dto.UserResponse;
import com.tuition.entity.Role;
import com.tuition.entity.User;
import com.tuition.entity.UserStatus;
import com.tuition.exception.BusinessException;
import com.tuition.repository.UserRepository;
import com.tuition.service.AuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Controller dành riêng cho Quản trị viên (ADMIN) quản lý người dùng trong hệ thống.
 */
@RestController
@RequestMapping("/api/admin/users")
@RequiredArgsConstructor
public class AdminUserController {

    private final AuthService authService;
    private final UserRepository userRepository;

    /**
     * Admin tạo tài khoản mới (cho phép tạo bất kỳ role nào: ADMIN, TEACHER, STUDENT)
     */
    @PostMapping
    public ResponseEntity<UserResponse> createUser(@Valid @RequestBody RegisterRequest req) {
        UserResponse response = authService.createByAdmin(req);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    /**
     * Admin lấy danh sách người dùng (có thể lọc theo role)
     */
    @GetMapping
    public ResponseEntity<List<UserResponse>> getUsers(@RequestParam(required = false) Role role) {
        List<User> users;
        if (role != null) {
            users = userRepository.findByRole(role);
        } else {
            users = userRepository.findAll();
        }

        List<UserResponse> responses = users.stream()
                .map(UserResponse::fromEntity)
                .toList();

        return ResponseEntity.ok(responses);
    }

    /**
     * Cập nhật trạng thái người dùng (ACTIVE / INACTIVE)
     */
    @PutMapping("/{id}/status")
    public ResponseEntity<UserResponse> updateStatus(
            @PathVariable Long id,
            @RequestParam UserStatus status
    ) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new BusinessException("Không tìm thấy người dùng có ID: " + id));

        user.setStatus(status);
        User updatedUser = userRepository.save(user);

        return ResponseEntity.ok(UserResponse.fromEntity(updatedUser));
    }

    /**
     * Xóa mềm người dùng (Soft Delete: đổi status thành INACTIVE)
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<UserResponse> softDeleteUser(@PathVariable Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new BusinessException("Không tìm thấy người dùng có ID: " + id));

        user.setStatus(UserStatus.INACTIVE);
        User updatedUser = userRepository.save(user);

        return ResponseEntity.ok(UserResponse.fromEntity(updatedUser));
    }
}
