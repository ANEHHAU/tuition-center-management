package com.tuition;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.tuition.dto.ChangePasswordRequest;
import com.tuition.dto.LoginRequest;
import com.tuition.dto.RegisterRequest;
import com.tuition.dto.UpdateProfileRequest;
import com.tuition.entity.Role;
import com.tuition.entity.User;
import com.tuition.entity.UserStatus;
import com.tuition.repository.UserRepository;
import com.tuition.security.JwtService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
public class TuitionApplicationTests {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private JwtService jwtService;

    private String adminToken;
    private String studentToken;

    @BeforeEach
    void setUp() {
        userRepository.deleteAll();

        // Tạo Admin hoặc lấy từ DB
        User admin = userRepository.findByUsername("admin").orElseGet(() -> userRepository.save(User.builder()
                .username("admin")
                .password(passwordEncoder.encode("admin123"))
                .role(Role.ADMIN)
                .fullName("System Admin")
                .email("admin_test@tuition.com")
                .status(UserStatus.ACTIVE)
                .build()));
        adminToken = jwtService.generateToken(admin);

        // Tạo Student
        User student = userRepository.save(User.builder()
                .username("student1")
                .password(passwordEncoder.encode("password123"))
                .role(Role.STUDENT)
                .fullName("Student One")
                .email("student1@tuition.com")
                .status(UserStatus.ACTIVE)
                .build());
        studentToken = jwtService.generateToken(student);
    }

    @Test
    @DisplayName("Test 1: Register STUDENT thành công -> 201")
    void test1_registerStudentSuccess() throws Exception {
        RegisterRequest req = new RegisterRequest(
                "newstudent", "password123", Role.STUDENT,
                "New Student", "newstudent@tuition.com", "0900000000",
                "Hanoi", "Note", null
        );

        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.username", is("newstudent")))
                .andExpect(jsonPath("$.role", is("STUDENT")));
    }

    @Test
    @DisplayName("Test 2: Register với username trùng -> 400")
    void test2_registerDuplicateUsername() throws Exception {
        RegisterRequest req = new RegisterRequest(
                "student1", "password123", Role.STUDENT,
                "Student Duplicate", "dup@tuition.com", null, null, null, null
        );

        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message", containsString("Username")));
    }

    @Test
    @DisplayName("Test 3: Register với role ADMIN -> 400 Không thể tự đăng ký ADMIN")
    void test3_registerAdminSelfDenied() throws Exception {
        RegisterRequest req = new RegisterRequest(
                "hackeradmin", "password123", Role.ADMIN,
                "Hacker Admin", "hacker@tuition.com", null, null, null, null
        );

        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message", containsString("Không thể tự đăng ký tài khoản ADMIN")));
    }

    @Test
    @DisplayName("Test 4: Login đúng -> 200 + token")
    void test4_loginSuccess() throws Exception {
        LoginRequest req = new LoginRequest("student1", "password123");

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token", notNullValue()))
                .andExpect(jsonPath("$.username", is("student1")));
    }

    @Test
    @DisplayName("Test 5: Login sai -> 401")
    void test5_loginWrongPassword() throws Exception {
        LoginRequest req = new LoginRequest("student1", "wrongpass");

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("Test 6: Login user INACTIVE -> 401/400")
    void test6_loginInactiveUser() throws Exception {
        User inactive = userRepository.save(User.builder()
                .username("inactiveuser")
                .password(passwordEncoder.encode("password123"))
                .role(Role.STUDENT)
                .fullName("Inactive User")
                .status(UserStatus.INACTIVE)
                .build());

        LoginRequest req = new LoginRequest("inactiveuser", "password123");

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("Test 7: Gọi /api/admin/** bằng token STUDENT -> 403")
    void test7_accessAdminEndpointWithStudentToken() throws Exception {
        mockMvc.perform(get("/api/admin/users")
                        .header("Authorization", "Bearer " + studentToken))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("Test 8: Gọi /api/profile không token -> 401")
    void test8_accessProfileWithoutToken() throws Exception {
        mockMvc.perform(get("/api/profile"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("Test 9: Gọi /api/profile có token -> 200 + info")
    void test9_accessProfileWithToken() throws Exception {
        mockMvc.perform(get("/api/profile")
                        .header("Authorization", "Bearer " + studentToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.username", is("student1")))
                .andExpect(jsonPath("$.role", is("STUDENT")));
    }

    @Test
    @DisplayName("Test 10: Update profile -> 200")
    void test10_updateProfile() throws Exception {
        UpdateProfileRequest req = new UpdateProfileRequest(
                "Updated Student Name", "updated@tuition.com", "0988888888", "Updated Address", "New Note", null
        );

        mockMvc.perform(put("/api/profile")
                        .header("Authorization", "Bearer " + studentToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.fullName", is("Updated Student Name")))
                .andExpect(jsonPath("$.email", is("updated@tuition.com")));
    }

    @Test
    @DisplayName("Test 11: Change password với mật khẩu cũ đúng -> 200")
    void test11_changePasswordSuccess() throws Exception {
        ChangePasswordRequest req = new ChangePasswordRequest("password123", "newpassword123");

        mockMvc.perform(put("/api/profile/password")
                        .header("Authorization", "Bearer " + studentToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message", containsString("thành công")));
    }

    @Test
    @DisplayName("Test 12: Change password với mật khẩu cũ sai -> 400")
    void test12_changePasswordWrongOldPassword() throws Exception {
        ChangePasswordRequest req = new ChangePasswordRequest("wrongoldpassword", "newpassword123");

        mockMvc.perform(put("/api/profile/password")
                        .header("Authorization", "Bearer " + studentToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message", containsString("Mật khẩu cũ không chính xác")));
    }

    @Test
    @DisplayName("Test 13: Upload avatar > 2MB -> 400")
    void test13_uploadAvatarOverSize() throws Exception {
        byte[] largeContent = new byte[3 * 1024 * 1024]; // 3MB
        MockMultipartFile file = new MockMultipartFile("file", "avatar.jpg", "image/jpeg", largeContent);

        mockMvc.perform(multipart("/api/files/avatar")
                        .file(file)
                        .header("Authorization", "Bearer " + studentToken))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message", containsString("2MB")));
    }

    @Test
    @DisplayName("Test 14: Admin tạo TEACHER qua /api/admin/users -> 201")
    void test14_adminCreateTeacher() throws Exception {
        RegisterRequest req = new RegisterRequest(
                "teacher1", "password123", Role.TEACHER,
                "Teacher One", "teacher1@tuition.com", "0911111111",
                "Danang", "Math Teacher", null
        );

        mockMvc.perform(post("/api/admin/users")
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.username", is("teacher1")))
                .andExpect(jsonPath("$.role", is("TEACHER")));
    }

    @Autowired
    private com.tuition.repository.GroupRepository groupRepository;

    @Autowired
    private com.tuition.repository.CourseRepository courseRepository;

    @Autowired
    private com.tuition.service.PublicLinkService publicLinkService;

    @Test
    @DisplayName("Test 15: Upload file .exe đổi tên thành .jpg -> 400 (Magic bytes check)")
    void test15_uploadFakeJpgFile() throws Exception {
        byte[] exeHeaderBytes = new byte[]{(byte)0x4D, (byte)0x5A, (byte)0x90, 0x00, 0x03, 0x00, 0x00, 0x00}; // MZ header (EXE)
        MockMultipartFile fakeJpgFile = new MockMultipartFile("file", "fake.jpg", "image/jpeg", exeHeaderBytes);

        mockMvc.perform(multipart("/api/files/avatar")
                        .file(fakeJpgFile)
                        .header("Authorization", "Bearer " + studentToken))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message", containsString("không phải là")));
    }

    private com.tuition.entity.Course createDummyCourse(String name) {
        User teacher = userRepository.findByUsername("admin").orElseThrow();
        return courseRepository.save(com.tuition.entity.Course.builder()
                .name(name)
                .pricePerSession(new java.math.BigDecimal("100000"))
                .teacher(teacher)
                .build());
    }

    @Test
    @DisplayName("Test 16: Public schedule token creation & view -> 200, NO price in HTML")
    void test16_publicScheduleSuccess() throws Exception {
        com.tuition.entity.Course course = createDummyCourse("Toán Nâng Cao");
        com.tuition.entity.Group group = groupRepository.save(com.tuition.entity.Group.builder()
                .name("Nhóm Toán 10A")
                .course(course)
                .teacher(course.getTeacher())
                .build());

        String token = publicLinkService.generateToken(group);

        mockMvc.perform(get("/public/schedule/" + token))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("Nhóm Toán 10A")))
                .andExpect(content().string(not(containsString("học phí"))))
                .andExpect(content().string(not(containsString("VND"))))
                .andExpect(content().string(not(containsString("Giá tiền"))));
    }

    @Test
    @DisplayName("Test 17: Revoked public token -> 400 Error")
    void test17_revokedPublicScheduleToken() throws Exception {
        com.tuition.entity.Course course = createDummyCourse("Vật Lý 11");
        com.tuition.entity.Group group = groupRepository.save(com.tuition.entity.Group.builder()
                .name("Nhóm Lý 11B")
                .course(course)
                .teacher(course.getTeacher())
                .build());

        String token = publicLinkService.generateToken(group);
        publicLinkService.revokeToken(group);

        mockMvc.perform(get("/public/schedule/" + token))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message", containsString("ngắt chia sẻ")));
    }

    @Test
    @DisplayName("Test 18: Expired public token -> 400 Error")
    void test18_expiredPublicScheduleToken() throws Exception {
        com.tuition.entity.Course course = createDummyCourse("Hóa Học 12");
        com.tuition.entity.Group group = groupRepository.save(com.tuition.entity.Group.builder()
                .name("Nhóm Hóa 12C")
                .course(course)
                .teacher(course.getTeacher())
                .build());

        String token = publicLinkService.generateToken(group);
        group.setTokenExpiresAt(java.time.LocalDateTime.now().minusDays(1));
        groupRepository.save(group);

        mockMvc.perform(get("/public/schedule/" + token))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message", containsString("hết hạn")));
    }

}
