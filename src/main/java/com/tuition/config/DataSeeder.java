package com.tuition.config;

import com.tuition.entity.Role;
import com.tuition.entity.User;
import com.tuition.entity.UserStatus;
import com.tuition.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

/**
 * Tự động khởi tạo dữ liệu mặc định khi ứng dụng khởi chạy.
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class DataSeeder implements CommandLineRunner {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) {
        if (!userRepository.existsByUsername("admin")) {
            User admin = User.builder()
                    .username("admin")
                    .password(passwordEncoder.encode("admin123"))
                    .role(Role.ADMIN)
                    .fullName("System Administrator")
                    .email("admin@tuition.com")
                    .phone("0901234567")
                    .address("Hệ thống trung tâm dạy thêm")
                    .note("Tài khoản quản trị mặc định")
                    .status(UserStatus.ACTIVE)
                    .build();

            userRepository.save(admin);
            log.info(">>> Đã khởi tạo thành công tài khoản ADMIN mặc định (username: admin, password: admin123)");
        }
    }
}
