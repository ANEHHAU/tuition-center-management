package com.tuition.service;

import com.tuition.dto.AuthResponse;
import com.tuition.dto.LoginRequest;
import com.tuition.dto.RegisterRequest;
import com.tuition.dto.UserResponse;
import com.tuition.entity.Role;
import com.tuition.entity.User;
import com.tuition.entity.UserStatus;
import com.tuition.exception.BusinessException;
import com.tuition.repository.UserRepository;
import com.tuition.security.JwtService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Service xử lý nghiệp vụ Đăng ký, Đăng nhập và Quản trị tài khoản.
 */
@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final AuthenticationManager authenticationManager;

    /**
     * Đăng ký người dùng công khai (Public Register)
     * Quy tắc: KHÔNG cho phép tự đăng ký role ADMIN.
     */
    @Transactional
    public UserResponse register(RegisterRequest req) {
        if (req.role() == Role.ADMIN) {
            throw new BusinessException("Không thể tự đăng ký tài khoản ADMIN");
        }

        return createUserInternal(req);
    }

    /**
     * Admin tạo người dùng (Admin User Creation)
     * Cho phép khởi tạo bất kỳ Role nào (kể cả ADMIN).
     */
    @Transactional
    public UserResponse createByAdmin(RegisterRequest req) {
        return createUserInternal(req);
    }

    /**
     * Hàm nội bộ kiểm tra trùng lặp và lưu User mới.
     */
    private UserResponse createUserInternal(RegisterRequest req) {
        if (userRepository.existsByUsername(req.username())) {
            throw new BusinessException("Username đã tồn tại trong hệ thống");
        }

        if (req.email() != null && !req.email().isBlank() && userRepository.existsByEmail(req.email())) {
            throw new BusinessException("Email đã tồn tại trong hệ thống");
        }

        User user = User.builder()
                .username(req.username())
                .password(passwordEncoder.encode(req.password()))
                .role(req.role())
                .fullName(req.fullName())
                .email(req.email())
                .phone(req.phone())
                .address(req.address())
                .note(req.note())
                .avatarUrl(req.avatarUrl())
                .status(UserStatus.ACTIVE)
                .build();

        User savedUser = userRepository.save(user);
        return UserResponse.fromEntity(savedUser);
    }

    /**
     * Đăng nhập ứng dụng và sinh token JWT
     */
    public AuthResponse login(LoginRequest req) {
        // Xác thực username & password
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(req.username(), req.password())
        );

        User user = userRepository.findByUsername(req.username())
                .orElseThrow(() -> new BusinessException("Sai tài khoản hoặc mật khẩu"));

        if (user.getStatus() == UserStatus.INACTIVE) {
            throw new BusinessException("Tài khoản của bạn đã bị khóa hoặc ngừng hoạt động");
        }

        String token = jwtService.generateToken(user);

        return new AuthResponse(
                token,
                user.getUsername(),
                user.getRole(),
                user.getId(),
                user.getFullName(),
                user.getAvatarUrl()
        );
    }
}
