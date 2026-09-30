package com.tuition.config;

import com.tuition.entity.*;
import com.tuition.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;

/**
 * DataSeeder dùng để tạo dữ liệu mẫu cho môi trường dev.
 * Chỉ chạy khi active profile là "dev".
 */
@Component
@Profile("dev")
@RequiredArgsConstructor
@Slf4j
public class DataSeeder implements CommandLineRunner {

    private final UserRepository userRepository;
    private final CourseRepository courseRepository;
    private final GroupRepository groupRepository;
    private final EnrollmentRepository enrollmentRepository;
    private final SessionRepository sessionRepository;
    private final AttendanceRepository attendanceRepository;
    private final InvoiceRepository invoiceRepository;
    private final PaymentRepository paymentRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    @Transactional
    public void run(String... args) {
        if (userRepository.existsByUsername("teacher1")) {
            log.info(">>> DataSeeder: Dữ liệu mẫu đã tồn tại (teacher1). Bỏ qua seed data.");
            return;
        }

        log.info(">>> Bắt đầu tạo dữ liệu Seed cho môi trường DEV...");

        // 1. Tạo Users
        User admin = userRepository.findByUsername("admin").orElseGet(() -> 
            createUser("admin", "admin123", "System Administrator", null, null, Role.ADMIN)
        );

        User teacher1 = createUser("teacher1", "123456", "Nguyễn Văn An", "an@tuition.com", "0901111111", Role.TEACHER);
        User teacher2 = createUser("teacher2", "123456", "Trần Thị Bình", "binh@tuition.com", "0902222222", Role.TEACHER);
        User teacher3 = createUser("teacher3", "123456", "Lê Minh Cường", "cuong@tuition.com", "0903333333", Role.TEACHER);

        List<User> students = new ArrayList<>();
        students.add(createUser("student1", "123456", "Đỗ Văn An", "student1@tuition.com", "0911000001", Role.STUDENT));
        students.add(createUser("student2", "123456", "Dương Thị Bình", "student2@tuition.com", "0911000002", Role.STUDENT));
        students.add(createUser("student3", "123456", "Hoàng Minh Cường", "student3@tuition.com", "0911000003", Role.STUDENT));
        students.add(createUser("student4", "123456", "Lý Thị Dung", "student4@tuition.com", "0911000004", Role.STUDENT));
        students.add(createUser("student5", "123456", "Phạm Văn Dũng", "student5@tuition.com", "0911000005", Role.STUDENT));
        students.add(createUser("student6", "123456", "Vũ Thị Hà", "student6@tuition.com", "0911000006", Role.STUDENT));
        students.add(createUser("student7", "123456", "Bùi Văn Hùng", "student7@tuition.com", "0911000007", Role.STUDENT));
        students.add(createUser("student8", "123456", "Ngô Thị Lan", "student8@tuition.com", "0911000008", Role.STUDENT));
        students.add(createUser("student9", "123456", "Trịnh Văn Long", "student9@tuition.com", "0911000009", Role.STUDENT));
        students.add(createUser("student10", "123456", "Mai Thị Mai", "student10@tuition.com", "0911000010", Role.STUDENT));

        log.info(">>> Đã tạo 1 Admin, 3 Teachers, 10 Students.");

        // 2. Tạo Courses
        Course course1 = createCourse("Toán 9 cơ bản", 100000, teacher1);
        Course course2 = createCourse("Toán 9 nâng cao", 150000, teacher1);
        Course course3 = createCourse("Lý 10", 120000, teacher2);
        Course course4 = createCourse("Hóa 11", 130000, teacher3);
        log.info(">>> Đã tạo 4 Courses.");

        // 3. Tạo Groups
        Group groupT9A = createGroup("Toán 9A", course1, teacher1, "token-t9a");
        Group groupT9B = createGroup("Toán 9B", course1, teacher1, "token-t9b");
        Group groupT9NC = createGroup("Toán 9 NC", course2, teacher1, "token-t9nc");
        Group groupL10A = createGroup("Lý 10A", course3, teacher2, "token-l10a");
        Group groupL10B = createGroup("Lý 10B", course3, teacher2, "token-l10b");
        Group groupH11A = createGroup("Hóa 11A", course4, teacher3, "token-h11a");
        log.info(">>> Đã tạo 6 Groups.");

        // 4. Enrollments
        enrollStudent(students.get(0), groupT9A, LocalDate.of(2026, 9, 1), null); // student1 học Toán 9A
        enrollStudent(students.get(0), groupL10A, LocalDate.of(2026, 9, 1), null); // student1 học cả Lý 10A

        // student2 học Toán 9A từ 1/9 đến 15/10, sau đó chuyển sang Toán 9B
        enrollStudent(students.get(1), groupT9A, LocalDate.of(2026, 9, 1), LocalDate.of(2026, 10, 15));
        enrollStudent(students.get(1), groupT9B, LocalDate.of(2026, 10, 16), null);

        // Add 3 more students to T9A
        enrollStudent(students.get(2), groupT9A, LocalDate.of(2026, 9, 1), null);
        enrollStudent(students.get(3), groupT9A, LocalDate.of(2026, 9, 1), null);
        enrollStudent(students.get(4), groupT9A, LocalDate.of(2026, 9, 1), null);

        // Add students to T9B, T9NC, L10A, L10B, H11A...
        enrollStudent(students.get(5), groupT9B, LocalDate.of(2026, 9, 1), null);
        enrollStudent(students.get(6), groupT9B, LocalDate.of(2026, 9, 1), null);

        enrollStudent(students.get(7), groupT9NC, LocalDate.of(2026, 9, 1), null);
        enrollStudent(students.get(8), groupT9NC, LocalDate.of(2026, 9, 1), null);
        enrollStudent(students.get(9), groupT9NC, LocalDate.of(2026, 9, 1), null);
        enrollStudent(students.get(2), groupT9NC, LocalDate.of(2026, 9, 1), null); // student3 học 2 lớp

        enrollStudent(students.get(3), groupL10A, LocalDate.of(2026, 9, 1), null);
        enrollStudent(students.get(4), groupL10A, LocalDate.of(2026, 9, 1), null);
        enrollStudent(students.get(5), groupL10A, LocalDate.of(2026, 9, 1), null);
        enrollStudent(students.get(6), groupL10A, LocalDate.of(2026, 9, 1), null);

        enrollStudent(students.get(7), groupL10B, LocalDate.of(2026, 9, 1), null);
        enrollStudent(students.get(8), groupL10B, LocalDate.of(2026, 9, 1), null);
        enrollStudent(students.get(9), groupL10B, LocalDate.of(2026, 9, 1), null);

        enrollStudent(students.get(0), groupH11A, LocalDate.of(2026, 9, 1), null);
        enrollStudent(students.get(1), groupH11A, LocalDate.of(2026, 9, 1), null);
        enrollStudent(students.get(2), groupH11A, LocalDate.of(2026, 9, 1), null);
        enrollStudent(students.get(3), groupH11A, LocalDate.of(2026, 9, 1), null);

        log.info(">>> Đã tạo Enrollments.");

        // 5. Sessions & Attendance
        List<Group> allGroups = Arrays.asList(groupT9A, groupT9B, groupT9NC, groupL10A, groupL10B, groupH11A);
        LocalDate startDate = LocalDate.now().minusMonths(3);
        LocalDate endDate = LocalDate.now();
        Random random = new Random();

        for (Group group : allGroups) {
            LocalDate currentDate = startDate;
            int dayCounter = 0;
            while (currentDate.isBefore(endDate)) {
                // Tạo session 2 buổi/tuần (thứ 3, thứ 6)
                if (currentDate.getDayOfWeek().getValue() == 3 || currentDate.getDayOfWeek().getValue() == 6) {
                    Session session = new Session();
                    session.setGroup(group);
                    session.setDate(currentDate);
                    session.setStartTime(LocalTime.of(18, 0));
                    session.setEndTime(LocalTime.of(19, 30));
                    session.setRoom("Phòng " + (random.nextInt(5) + 1));
                    
                    // Để 1 số buổi gần đây chưa điểm danh (trạng thái SCHEDULED)
                    boolean isRecent = currentDate.isAfter(endDate.minusDays(7));
                    if (isRecent) {
                        session.setStatus(SessionStatus.SCHEDULED);
                    } else {
                        // 5% bị hủy
                        if (random.nextInt(100) < 5) {
                            session.setStatus(SessionStatus.CANCELLED);
                        } else {
                            session.setStatus(SessionStatus.COMPLETED);
                        }
                    }
                    session = sessionRepository.save(session);

                    if (session.getStatus() == SessionStatus.COMPLETED) {
                        // Lấy học sinh active trong ngày này
                        final LocalDate sDate = session.getDate();
                        List<Enrollment> activeEnrollments = enrollmentRepository.findByGroupId(group.getId()).stream()
                                .filter(e -> !e.getJoinDate().isAfter(sDate) && 
                                        (e.getLeaveDate() == null || !e.getLeaveDate().isBefore(sDate)))
                                .toList();

                        for (Enrollment e : activeEnrollments) {
                            Attendance attendance = new Attendance();
                            attendance.setSession(session);
                            attendance.setStudent(e.getStudent());
                            attendance.setPriceSnapshot(group.getCourse().getPricePerSession());
                            
                            int rand = random.nextInt(100);
                            if (rand < 70) attendance.setStatus(AttendanceStatus.PRESENT);
                            else if (rand < 80) attendance.setStatus(AttendanceStatus.ABSENT);
                            else if (rand < 95) attendance.setStatus(AttendanceStatus.EXCUSED);
                            else attendance.setStatus(AttendanceStatus.CANCELLED);
                            
                            attendanceRepository.save(attendance);
                        }
                    }
                }
                currentDate = currentDate.plusDays(1);
            }
        }
        log.info(">>> Đã tạo Sessions & Attendances.");

        // 6. Invoices & Payments
        // Tháng trước
        int lastMonth = LocalDate.now().minusMonths(1).getMonthValue();
        int lastMonthYear = LocalDate.now().minusMonths(1).getYear();

        int invoiceCount = 0;
        for (User student : students) {
            Invoice invoice = new Invoice();
            invoice.setStudent(student);
            invoice.setMonth(lastMonth);
            invoice.setYear(lastMonthYear);
            invoice.setTotalSessions(8);
            
            // Random tổng tiền
            BigDecimal totalAmount = BigDecimal.valueOf(1000000 + random.nextInt(2000000));
            invoice.setTotalAmount(totalAmount);
            
            int statusRand = random.nextInt(3);
            if (statusRand == 0) {
                invoice.setStatus(InvoiceStatus.UNPAID);
                invoice.setPaidAmount(BigDecimal.ZERO);
            } else if (statusRand == 1) {
                invoice.setStatus(InvoiceStatus.PARTIAL);
                BigDecimal paid = totalAmount.divide(BigDecimal.valueOf(2));
                invoice.setPaidAmount(paid);
            } else {
                invoice.setStatus(InvoiceStatus.PAID);
                invoice.setPaidAmount(totalAmount);
            }
            
            invoice.setInvoiceStatus(random.nextBoolean() ? InvoiceDocumentStatus.FINALIZED : InvoiceDocumentStatus.DRAFT);
            invoice.setGeneratedAt(LocalDateTime.now().minusDays(random.nextInt(15)));
            
            invoice = invoiceRepository.save(invoice);
            invoiceCount++;

            // Thêm Payments
            if (invoice.getStatus() == InvoiceStatus.PARTIAL || invoice.getStatus() == InvoiceStatus.PAID) {
                Payment payment = new Payment();
                payment.setInvoice(invoice);
                payment.setAmount(invoice.getPaidAmount());
                payment.setPaidAt(LocalDateTime.now().minusDays(random.nextInt(10)));
                payment.setMethod(random.nextBoolean() ? PaymentMethod.CASH : PaymentMethod.BANK_TRANSFER);
                payment.setNote("Đóng tiền tháng " + lastMonth);
                payment.setRecordedBy(admin);
                paymentRepository.save(payment);
            }
        }
        log.info(">>> Đã tạo {} Invoices và Payments.", invoiceCount);

        log.info(">>> HOÀN THÀNH TẠO DỮ LIỆU SEED!");
    }

    private User createUser(String username, String pass, String name, String email, String phone, Role role) {
        User u = new User();
        u.setUsername(username);
        u.setPassword(passwordEncoder.encode(pass));
        u.setFullName(name);
        u.setEmail(email);
        u.setPhone(phone);
        u.setRole(role);
        u.setStatus(UserStatus.ACTIVE);
        return userRepository.save(u);
    }

    private Course createCourse(String name, int price, User teacher) {
        Course c = new Course();
        c.setName(name);
        c.setPricePerSession(BigDecimal.valueOf(price));
        c.setTeacher(teacher);
        c.setStatus(CourseStatus.ACTIVE);
        return courseRepository.save(c);
    }

    private Group createGroup(String name, Course course, User teacher, String token) {
        Group g = new Group();
        g.setName(name);
        g.setCourse(course);
        g.setTeacher(teacher);
        g.setStartDate(LocalDate.of(2026, 9, 1));
        g.setStatus(GroupStatus.ACTIVE);
        g.setPublicToken(token);
        return groupRepository.save(g);
    }

    private void enrollStudent(User student, Group group, LocalDate start, LocalDate end) {
        Enrollment e = new Enrollment();
        e.setStudent(student);
        e.setGroup(group);
        e.setJoinDate(start);
        e.setLeaveDate(end);
        e.setStatus(end == null ? EnrollmentStatus.ACTIVE : EnrollmentStatus.LEFT);
        enrollmentRepository.save(e);
    }
}
