package com.tuition.integration;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.tuition.dto.CourseRequest;
import com.tuition.dto.RegisterRequest;
import com.tuition.dto.UpdateUserRequest;
import com.tuition.entity.Course;
import com.tuition.entity.CourseStatus;
import com.tuition.entity.Role;
import com.tuition.entity.User;
import com.tuition.repository.CourseRepository;
import com.tuition.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
public class TeacherIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private CourseRepository courseRepository;

    private User teacher1;
    private User teacher2;
    private User studentT1;
    private User studentT2;

    @BeforeEach
    void setUp() {
        teacher1 = new User();
        teacher1.setUsername("teacher1");
        teacher1.setRole(Role.TEACHER);
        teacher1.setPassword("pass");
        teacher1.setFullName("Teacher 1");
        teacher1 = userRepository.save(teacher1);

        teacher2 = new User();
        teacher2.setUsername("teacher2");
        teacher2.setRole(Role.TEACHER);
        teacher2.setPassword("pass");
        teacher2.setFullName("Teacher 2");
        teacher2 = userRepository.save(teacher2);

        studentT1 = new User();
        studentT1.setUsername("studentT1");
        studentT1.setRole(Role.STUDENT);
        studentT1.setPassword("pass");
        studentT1.setFullName("Student of T1");
        studentT1.setCreatedById(teacher1.getId());
        studentT1 = userRepository.save(studentT1);

        studentT2 = new User();
        studentT2.setUsername("studentT2");
        studentT2.setRole(Role.STUDENT);
        studentT2.setPassword("pass");
        studentT2.setFullName("Student of T2");
        studentT2.setCreatedById(teacher2.getId());
        studentT2 = userRepository.save(studentT2);

        Course courseT1 = new Course();
        courseT1.setName("Math T1");
        courseT1.setPricePerSession(BigDecimal.valueOf(100));
        courseT1.setTeacher(teacher1);
        courseT1.setStatus(CourseStatus.ACTIVE);
        courseRepository.save(courseT1);
    }

    @Test
    @WithMockUser(username = "teacher1", roles = {"TEACHER"})
    void shouldGetOnlyOwnCourses() throws Exception {
        mockMvc.perform(get("/api/teacher/courses"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", hasSize(1)))
                .andExpect(jsonPath("$.content[0].name", is("Math T1")));
    }

    @Test
    @WithMockUser(username = "teacher1", roles = {"TEACHER"})
    void shouldCreateCourseSuccessfully() throws Exception {
        CourseRequest req = new CourseRequest(
            "Physics T1", BigDecimal.valueOf(150000), "Desc", null, null, CourseStatus.ACTIVE, null
        );

        mockMvc.perform(post("/api/teacher/courses")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.name", is("Physics T1")));
    }

    @Test
    @WithMockUser(username = "teacher1", roles = {"TEACHER"})
    void shouldSearchStudentsWithKeyword() throws Exception {
        mockMvc.perform(get("/api/teacher/students?keyword=T1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", hasSize(1)))
                .andExpect(jsonPath("$.content[0].username", is("studentT1")));
    }

    @Test
    @WithMockUser(username = "teacher1", roles = {"TEACHER"})
    void shouldSortStudentsByName() throws Exception {
        User studentT1_2 = new User();
        studentT1_2.setUsername("studentT1_2");
        studentT1_2.setRole(Role.STUDENT);
        studentT1_2.setPassword("pass");
        studentT1_2.setFullName("Albert Einstein");
        studentT1_2.setCreatedById(teacher1.getId());
        userRepository.save(studentT1_2);

        mockMvc.perform(get("/api/teacher/students?sortBy=name&sortDir=asc"))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser(username = "teacher1", roles = {"TEACHER"})
    void shouldCreateStudentSuccessfully() throws Exception {
        RegisterRequest req = new RegisterRequest(
            "newStudent", "password", Role.STUDENT, "New Student", "new@gmail.com", "0123", "address", null, null
        );

        mockMvc.perform(post("/api/teacher/students")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.username", is("newStudent")));
    }

    @Test
    @WithMockUser(username = "teacher1", roles = {"TEACHER"})
    void shouldReturn403WhenUpdatingOtherTeacherStudent() throws Exception {
        UpdateUserRequest req = new UpdateUserRequest(
            "Hacked", null, null, null, null, null
        );

        mockMvc.perform(put("/api/teacher/students/" + studentT2.getId())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @WithMockUser(username = "teacher1", roles = {"TEACHER"})
    void shouldReturn403WhenCallingAdminEndpoint() throws Exception {
        mockMvc.perform(get("/api/admin/users"))
                .andExpect(status().isForbidden());
    }
}
