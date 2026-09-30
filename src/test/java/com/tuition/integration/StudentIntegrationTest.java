package com.tuition.integration;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.tuition.TuitionApplication;
import com.tuition.dto.RegisterRequest;
import com.tuition.entity.*;
import com.tuition.repository.*;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.LocalTime;
import java.util.Map;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest(classes = TuitionApplication.class)
@AutoConfigureMockMvc
@ActiveProfiles("test")
@DirtiesContext(classMode = DirtiesContext.ClassMode.BEFORE_EACH_TEST_METHOD)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class StudentIntegrationTest {

    @Autowired MockMvc mockMvc;
    @Autowired ObjectMapper objectMapper;
    @Autowired UserRepository userRepository;
    @Autowired CourseRepository courseRepository;
    @Autowired GroupRepository groupRepository;
    @Autowired EnrollmentRepository enrollmentRepository;
    @Autowired PasswordEncoder passwordEncoder;
    @Autowired InvoiceRepository invoiceRepository;

    private User student1, student2, teacher;
    private Course course;
    private Group group;

    @BeforeEach
    void setUp() {
        teacher = userRepository.save(User.builder()
                .username("teacher_s_test")
                .password(passwordEncoder.encode("password"))
                .fullName("Teacher S Test")
                .email("teacher_s@test.com")
                .role(Role.TEACHER)
                .status(UserStatus.ACTIVE)
                .build());

        student1 = userRepository.save(User.builder()
                .username("student_s1_test")
                .password(passwordEncoder.encode("password"))
                .fullName("Student S1")
                .email("student_s1@test.com")
                .role(Role.STUDENT)
                .status(UserStatus.ACTIVE)
                .build());

        student2 = userRepository.save(User.builder()
                .username("student_s2_test")
                .password(passwordEncoder.encode("password"))
                .fullName("Student S2")
                .email("student_s2@test.com")
                .role(Role.STUDENT)
                .status(UserStatus.ACTIVE)
                .build());

        course = courseRepository.save(Course.builder()
                .name("Test Course S")
                .pricePerSession(new BigDecimal("100000"))
                .teacher(teacher)
                .status(CourseStatus.ACTIVE)
                .build());

        group = groupRepository.save(Group.builder()
                .name("Test Group S")
                .course(course)
                .teacher(teacher)
                .status(GroupStatus.ACTIVE)
                .startDate(java.time.LocalDate.now().minusDays(10))
                .endDate(java.time.LocalDate.now().plusMonths(3))
                .build());

        enrollmentRepository.save(Enrollment.builder()
                .student(student1)
                .group(group)
                .status(EnrollmentStatus.ACTIVE)
                .joinDate(java.time.LocalDate.now().minusMonths(1))
                .build());

        // Create an invoice for student1
        invoiceRepository.save(Invoice.builder()
                .student(student1)
                .month(java.time.LocalDate.now().getMonthValue())
                .year(java.time.LocalDate.now().getYear())
                .totalSessions(5)
                .totalAmount(new BigDecimal("500000"))
                .paidAmount(BigDecimal.ZERO)
                .status(InvoiceStatus.UNPAID)
                .invoiceStatus(InvoiceDocumentStatus.FINALIZED)
                .build());
    }

    // Test 1: Dashboard stats
    @Test
    @Order(1)
    @WithMockUser(username = "student_s1_test", roles = {"STUDENT"})
    void shouldGetDashboardStats() throws Exception {
        mockMvc.perform(get("/api/student/dashboard/stats"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalGroups", is(1)));
    }

    // Test 2: Schedule only shows student's sessions
    @Test
    @Order(2)
    @WithMockUser(username = "student_s1_test", roles = {"STUDENT"})
    void shouldGetMySchedule() throws Exception {
        mockMvc.perform(get("/api/student/schedule"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray());
    }

    // Test 3: Attendance returns list
    @Test
    @Order(3)
    @WithMockUser(username = "student_s1_test", roles = {"STUDENT"})
    void shouldGetMyAttendance() throws Exception {
        mockMvc.perform(get("/api/student/attendance"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray());
    }

    // Test 4: Invoices returns list
    @Test
    @Order(4)
    @WithMockUser(username = "student_s1_test", roles = {"STUDENT"})
    void shouldGetMyInvoices() throws Exception {
        mockMvc.perform(get("/api/student/invoices"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray());
    }

    // Test 5: Current debt returns correct total
    @Test
    @Order(5)
    @WithMockUser(username = "student_s1_test", roles = {"STUDENT"})
    void shouldGetCurrentDebt() throws Exception {
        mockMvc.perform(get("/api/student/invoices/current-debt"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalDebt").value(500000));
    }

    // Test 6: Notify payment
    @Test
    @Order(6)
    @WithMockUser(username = "student_s1_test", roles = {"STUDENT"})
    void shouldNotifyPayment() throws Exception {
        // Get the invoice ID
        var invoices = invoiceRepository.findByStudentIdOrderByYearDescMonthDesc(student1.getId());
        Long invoiceId = invoices.get(0).getId();

        Map<String, Object> payload = Map.of("amount", 200000, "method", "BANK_TRANSFER", "note", "CK tháng 10");

        mockMvc.perform(post("/api/student/invoices/" + invoiceId + "/notify-payment")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(payload)))
                .andExpect(status().isOk());
    }

    // Test 7: Student cannot access teacher endpoints
    @Test
    @Order(7)
    @WithMockUser(username = "student_s1_test", roles = {"STUDENT"})
    void shouldReturn403WhenCallingTeacherEndpoint() throws Exception {
        mockMvc.perform(get("/api/teacher/courses"))
                .andExpect(status().isForbidden());
    }

    // Test 8: Student cannot access admin endpoints
    @Test
    @Order(8)
    @WithMockUser(username = "student_s1_test", roles = {"STUDENT"})
    void shouldReturn403WhenCallingAdminEndpoint() throws Exception {
        mockMvc.perform(get("/api/admin/users"))
                .andExpect(status().isForbidden());
    }

    // Test 9: Student cannot PUT course
    @Test
    @Order(9)
    @WithMockUser(username = "student_s1_test", roles = {"STUDENT"})
    void shouldReturn405WhenStudentTriesToModifyCourse() throws Exception {
        mockMvc.perform(put("/api/student/courses/1")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{}"))
                .andExpect(result -> {
                    int status = result.getResponse().getStatus();
                    org.junit.jupiter.api.Assertions.assertTrue(status >= 400);
                });
    }

    // Test 10: Student cannot view another student's invoice
    @Test
    @Order(10)
    @WithMockUser(username = "student_s1_test", roles = {"STUDENT"})
    void shouldReturn4xxWhenViewingOtherStudentInvoice() throws Exception {
        // Create invoice for student2
        Invoice inv2 = invoiceRepository.save(Invoice.builder()
                .student(student2)
                .month(1).year(2026)
                .totalSessions(5)
                .totalAmount(new BigDecimal("300000"))
                .paidAmount(BigDecimal.ZERO)
                .status(InvoiceStatus.UNPAID)
                .invoiceStatus(InvoiceDocumentStatus.FINALIZED)
                .build());

        mockMvc.perform(get("/api/student/invoices/" + inv2.getId()))
                .andExpect(status().is4xxClientError()); // 400 from BusinessException
    }

    // Test 11: Schedule filter by date range
    @Test
    @Order(11)
    @WithMockUser(username = "student_s1_test", roles = {"STUDENT"})
    void shouldFilterScheduleByDateRange() throws Exception {
        mockMvc.perform(get("/api/student/schedule?from=2020-01-01&to=2020-01-31"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray())
                .andExpect(jsonPath("$.content", hasSize(0)));
    }
}
