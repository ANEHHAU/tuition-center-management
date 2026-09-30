package com.tuition.controller;

import com.tuition.dto.BaseSearchRequest;
import com.tuition.dto.PageResponse;
import com.tuition.dto.RegisterRequest;
import com.tuition.dto.UpdateProfileRequest;
import com.tuition.dto.UpdateUserRequest;
import com.tuition.dto.UserResponse;
import com.tuition.entity.Role;
import com.tuition.entity.UserStatus;
import com.tuition.service.AdminUserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.Map;

/**
 * Controller dành riêng cho Quản trị viên (ADMIN) quản lý người dùng trong hệ thống.
 */
@RestController
@RequestMapping("/api/admin/users")
@RequiredArgsConstructor
public class AdminUserController {

    private final AdminUserService adminUserService;

    @GetMapping
    public ResponseEntity<PageResponse<UserResponse>> searchUsers(
            @ModelAttribute BaseSearchRequest req,
            @RequestParam(required = false) Role role,
            @RequestParam(required = false) UserStatus status
    ) {
        return ResponseEntity.ok(adminUserService.searchUsers(req, role, status));
    }

    @GetMapping("/{id}")
    public ResponseEntity<UserResponse> getUser(@PathVariable Long id) {
        return ResponseEntity.ok(adminUserService.getUserById(id));
    }

    @PostMapping
    public ResponseEntity<UserResponse> createUser(@Valid @RequestBody RegisterRequest req) {
        return ResponseEntity.status(HttpStatus.CREATED).body(adminUserService.createUser(req));
    }

    @PutMapping("/{id}")
    public ResponseEntity<UserResponse> updateUser(@PathVariable Long id, @Valid @RequestBody UpdateUserRequest req) {
        return ResponseEntity.ok(adminUserService.updateUser(id, req));
    }

    @PutMapping("/{id}/role")
    public ResponseEntity<UserResponse> updateRole(@PathVariable Long id, @RequestParam Role role) {
        return ResponseEntity.ok(adminUserService.updateRole(id, role));
    }

    @PutMapping("/{id}/status")
    public ResponseEntity<UserResponse> updateStatus(
            @PathVariable Long id,
            @RequestParam UserStatus status,
            Principal principal
    ) {
        return ResponseEntity.ok(adminUserService.changeStatus(id, status, principal.getName()));
    }

    @PutMapping("/{id}/reset-password")
    public ResponseEntity<Map<String, String>> resetPassword(
            @PathVariable Long id,
            @RequestBody Map<String, String> body
    ) {
        adminUserService.resetPassword(id, body.get("newPassword"));
        return ResponseEntity.ok(Map.of("message", "Reset mật khẩu thành công"));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Map<String, String>> softDeleteUser(@PathVariable Long id, Principal principal) {
        adminUserService.softDelete(id, principal.getName());
        return ResponseEntity.ok(Map.of("message", "Xóa người dùng thành công"));
    }
}
