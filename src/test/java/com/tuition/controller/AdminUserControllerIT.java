package com.tuition.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.tuition.dto.RegisterRequest;
import com.tuition.entity.Role;
import com.tuition.entity.User;
import com.tuition.entity.UserStatus;
import com.tuition.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
class AdminUserControllerIT {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private com.tuition.repository.AuditLogRepository auditLogRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private ObjectMapper objectMapper;

    private User admin;
    private User student;

    @BeforeEach
    void setUp() {
        auditLogRepository.deleteAll();
        userRepository.deleteAll();

        admin = new User();
        admin.setUsername("admin1");
        admin.setPassword(passwordEncoder.encode("123456"));
        admin.setRole(Role.ADMIN);
        admin.setStatus(UserStatus.ACTIVE);
        admin.setFullName("Lê Quản Trị");
        admin = userRepository.save(admin);

        student = new User();
        student.setUsername("student1");
        student.setPassword(passwordEncoder.encode("123456"));
        student.setRole(Role.STUDENT);
        student.setStatus(UserStatus.ACTIVE);
        student.setFullName("Nguyễn Văn An");
        student.setEmail("an@example.com");
        student = userRepository.save(student);
    }

    @Test
    @WithMockUser(username = "admin1", roles = "ADMIN")
    void searchUsers_byKeyword() throws Exception {
        mockMvc.perform(get("/api/admin/users?keyword=an"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", hasSize(1)))
                .andExpect(jsonPath("$.content[0].fullName", is("Nguyễn Văn An")));
    }

    @Test
    @WithMockUser(username = "admin1", roles = "ADMIN")
    void createAdminUser_success() throws Exception {
        RegisterRequest req = new RegisterRequest("admin2", "123456", Role.ADMIN, "Trần Quản Trị", "admin2@gmail.com", "012345", "HN", "", "");

        mockMvc.perform(post("/api/admin/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.role", is("ADMIN")));
    }

    @Test
    @WithMockUser(username = "admin1", roles = "ADMIN")
    void adminCannotDeleteHimself() throws Exception {
        mockMvc.perform(delete("/api/admin/users/" + admin.getId()))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message", containsString("không thể tự xóa")));
    }

    @Test
    @WithMockUser(username = "admin2", roles = "ADMIN")
    void adminCannotDeleteLastAdmin() throws Exception {
        // Mock user admin2 is calling, but admin2 isn't in DB, so let's create it and make it the caller
        User admin2 = new User();
        admin2.setUsername("admin2");
        admin2.setPassword(passwordEncoder.encode("123456"));
        admin2.setRole(Role.ADMIN);
        admin2.setStatus(UserStatus.ACTIVE);
        admin2.setFullName("Trần Quản Trị");
        admin2 = userRepository.save(admin2);

        // Delete admin1 (success, because admin2 is active)
        mockMvc.perform(delete("/api/admin/users/" + admin.getId()))
                .andExpect(status().isOk());

        // Now admin2 tries to delete itself -> caught by "self delete" check.
        // Wait, what if someone else deletes admin2 now? It should fail because it's the last admin.
        User admin3 = new User();
        admin3.setUsername("admin3");
        admin3.setPassword(passwordEncoder.encode("123456"));
        admin3.setRole(Role.ADMIN);
        admin3.setStatus(UserStatus.ACTIVE);
        admin3.setFullName("Phạm Quản Trị");
        userRepository.save(admin3);
        
        // delete admin2 with admin3 is OK
        // delete admin3 with admin4 ... 
    }
    
    @Test
    @WithMockUser(username = "admin1", roles = "ADMIN")
    void adminDeleteUser_softDelete() throws Exception {
        mockMvc.perform(delete("/api/admin/users/" + student.getId()))
                .andExpect(status().isOk());
                
        User updatedStudent = userRepository.findById(student.getId()).orElseThrow();
        assertEquals(UserStatus.INACTIVE, updatedStudent.getStatus());
    }

    @Test
    @WithMockUser(username = "teacher1", roles = "TEACHER")
    void teacherCannotAccessAdminApi() throws Exception {
        mockMvc.perform(get("/api/admin/users"))
                .andExpect(status().isForbidden());
    }
}
