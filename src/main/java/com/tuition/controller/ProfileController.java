package com.tuition.controller;

import com.tuition.dto.ChangePasswordRequest;
import com.tuition.dto.UpdateProfileRequest;
import com.tuition.dto.UserResponse;
import com.tuition.service.ProfileService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.Map;

/**
 * Controller xem và cập nhật thông tin cá nhân của User hiện tại.
 */
@RestController
@RequestMapping("/api/profile")
@RequiredArgsConstructor
public class ProfileController {

    private final ProfileService profileService;

    /**
     * Lấy thông tin tài khoản hiện tại
     */
    @GetMapping
    public ResponseEntity<UserResponse> getProfile(Principal principal) {
        UserResponse response = profileService.getProfile(principal.getName());
        return ResponseEntity.ok(response);
    }

    /**
     * Cập nhật hồ sơ cá nhân
     */
    @PutMapping
    public ResponseEntity<UserResponse> updateProfile(
            @Valid @RequestBody UpdateProfileRequest req,
            Principal principal
    ) {
        UserResponse response = profileService.updateProfile(principal.getName(), req);
        return ResponseEntity.ok(response);
    }

    /**
     * Đổi mật khẩu
     */
    @PutMapping("/password")
    public ResponseEntity<Map<String, String>> changePassword(
            @Valid @RequestBody ChangePasswordRequest req,
            Principal principal
    ) {
        profileService.changePassword(principal.getName(), req);
        return ResponseEntity.ok(Map.of("message", "Đổi mật khẩu thành công"));
    }
}
