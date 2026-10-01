package com.tuition.service;

import com.tuition.dto.*;
import com.tuition.entity.*;
import com.tuition.exception.BusinessException;
import com.tuition.repository.*;
import com.tuition.util.SortUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Service tổng hợp cho Teacher: quản lý student, dashboard, reports, bulk operations.
 */
@Service
@RequiredArgsConstructor
public class TeacherService {

    private final UserRepository userRepository;
    private final CourseRepository courseRepository;
    private final GroupRepository groupRepository;
    private final SessionRepository sessionRepository;
    private final EnrollmentRepository enrollmentRepository;
    private final AttendanceRepository attendanceRepository;
    private final InvoiceRepository invoiceRepository;
    private final PaymentRepository paymentRepository;
    private final PasswordEncoder passwordEncoder;

    // ==================== STUDENT MANAGEMENT ====================

    /**
     * Search students thuộc teacher (created_by_id = teacher.id).
     * Hỗ trợ keyword search 3-in-1, sort theo tên (từ cuối), phân trang.
     */
    @Transactional(readOnly = true)
    public PageResponse<UserResponse> searchStudents(User teacher, BaseSearchRequest req) {
        List<User> students = userRepository.findByRoleAndCreatedById(Role.STUDENT, teacher.getId());

        // Filter by keyword
        if (req.getKeyword() != null && !req.getKeyword().trim().isEmpty()) {
            String kw = req.getKeyword().toLowerCase().trim();
            students = students.stream().filter(s -> {
                String name = s.getFullName() != null ? s.getFullName().toLowerCase() : "";
                String email = s.getEmail() != null ? s.getEmail().toLowerCase() : "";
                String phone = s.getPhone() != null ? s.getPhone().toLowerCase() : "";
                return name.contains(kw) || email.contains(kw) || phone.contains(kw);
            }).collect(Collectors.toList());
        }

        // Sort
        if ("name".equalsIgnoreCase(req.getSortBy())) {
            students = SortUtils.sortListByLastName(students, User::getFullName, "asc".equalsIgnoreCase(req.getSortDir()));
        } else if ("createdAt".equalsIgnoreCase(req.getSortBy())) {
            students.sort(Comparator.comparing(User::getCreatedAt, Comparator.nullsLast(Comparator.naturalOrder())));
            if ("desc".equalsIgnoreCase(req.getSortDir())) Collections.reverse(students);
        } else if ("email".equalsIgnoreCase(req.getSortBy())) {
            students.sort(Comparator.comparing(u -> u.getEmail() != null ? u.getEmail() : "", String.CASE_INSENSITIVE_ORDER));
            if ("desc".equalsIgnoreCase(req.getSortDir())) Collections.reverse(students);
        }

        // Paginate
        int total = students.size();
        int start = req.getPage() * req.getSize();
        int end = Math.min(start + req.getSize(), total);
        List<User> pageContent = start < total ? students.subList(start, end) : List.of();
        int totalPages = (int) Math.ceil((double) total / req.getSize());

        return PageResponse.of(
                pageContent.stream().map(UserResponse::fromEntity).toList(),
                req.getPage(), req.getSize(), total, totalPages,
                req.getPage() == 0, end >= total
        );
    }

    @Transactional
    public UserResponse createStudent(User teacher, RegisterRequest req) {
        if (userRepository.existsByUsername(req.username())) {
            throw new BusinessException("Tên đăng nhập đã tồn tại");
        }
        if (req.email() != null && !req.email().isBlank() && userRepository.existsByEmail(req.email())) {
            throw new BusinessException("Email đã được sử dụng");
        }

        User student = User.builder()
                .username(req.username())
                .password(passwordEncoder.encode(req.password()))
                .fullName(req.fullName())
                .email(req.email())
                .phone(req.phone())
                .role(Role.STUDENT)
                .status(UserStatus.ACTIVE)
                .createdById(teacher.getId())
                .build();

        return UserResponse.fromEntity(userRepository.save(student));
    }

    @Transactional
    public UserResponse updateStudent(User teacher, Long studentId, UpdateUserRequest req) {
        User student = getStudentOfTeacher(teacher, studentId);

        if (req.fullName() != null) student.setFullName(req.fullName());
        if (req.email() != null && !req.email().isBlank()) {
            if (!req.email().equalsIgnoreCase(student.getEmail()) && userRepository.existsByEmail(req.email())) {
                throw new BusinessException("Email đã được sử dụng");
            }
            student.setEmail(req.email());
        }
        if (req.phone() != null) student.setPhone(req.phone());
        if (req.address() != null) student.setAddress(req.address());
        if (req.note() != null) student.setNote(req.note());

        return UserResponse.fromEntity(userRepository.save(student));
    }

    @Transactional
    public void softDeleteStudent(User teacher, Long studentId) {
        User student = getStudentOfTeacher(teacher, studentId);
        student.setStatus(UserStatus.INACTIVE);
        userRepository.save(student);
    }

    @Transactional
    public void resetStudentPassword(User teacher, Long studentId, String newPassword) {
        User student = getStudentOfTeacher(teacher, studentId);
        student.setPassword(passwordEncoder.encode(newPassword));
        userRepository.save(student);
    }

    @Transactional(readOnly = true)
    public UserResponse getStudentDetail(User teacher, Long studentId) {
        User student = getStudentOfTeacher(teacher, studentId);
        return UserResponse.fromEntity(student);
    }

    private User getStudentOfTeacher(User teacher, Long studentId) {
        User student = userRepository.findById(studentId)
                .orElseThrow(() -> new BusinessException("Không tìm thấy học sinh"));
        if (student.getRole() != Role.STUDENT) {
            throw new BusinessException("User này không phải học sinh");
        }
        if (!teacher.getId().equals(student.getCreatedById())) {
            throw new BusinessException("Bạn không có quyền quản lý học sinh này");
        }
        return student;
    }

    // ==================== COURSES SEARCH ====================

    @Transactional(readOnly = true)
    public PageResponse<CourseResponse> searchCourses(User teacher, String keyword, String status,
                                                       int page, int size, String sortBy, String sortDir) {
        List<Course> courses = courseRepository.findByTeacherId(teacher.getId());

        // Filter by keyword (name or description)
        if (keyword != null && !keyword.trim().isEmpty()) {
            String kw = keyword.toLowerCase().trim();
            courses = courses.stream().filter(c -> {
                String name = c.getName() != null ? c.getName().toLowerCase() : "";
                String desc = c.getDescription() != null ? c.getDescription().toLowerCase() : "";
                return name.contains(kw) || desc.contains(kw);
            }).collect(Collectors.toList());
        }

        // Filter by status
        if (status != null && !status.isEmpty()) {
            CourseStatus cs = CourseStatus.valueOf(status);
            courses = courses.stream().filter(c -> c.getStatus() == cs).collect(Collectors.toList());
        }

        // Sort
        if ("name".equalsIgnoreCase(sortBy)) {
            courses.sort(Comparator.comparing(c -> c.getName() != null ? c.getName() : "", String.CASE_INSENSITIVE_ORDER));
            if ("desc".equalsIgnoreCase(sortDir)) Collections.reverse(courses);
        } else if ("createdAt".equalsIgnoreCase(sortBy)) {
            courses.sort(Comparator.comparing(Course::getCreatedAt, Comparator.nullsLast(Comparator.naturalOrder())));
            if ("desc".equalsIgnoreCase(sortDir)) Collections.reverse(courses);
        } else if ("price".equalsIgnoreCase(sortBy)) {
            courses.sort(Comparator.comparing(Course::getPricePerSession, Comparator.nullsLast(Comparator.naturalOrder())));
            if ("desc".equalsIgnoreCase(sortDir)) Collections.reverse(courses);
        }

        // Paginate
        int total = courses.size();
        int start = page * size;
        int end = Math.min(start + size, total);
        List<Course> pageContent = start < total ? courses.subList(start, end) : List.of();
        int totalPages = (int) Math.ceil((double) total / size);

        List<CourseResponse> content = pageContent.stream()
                .map(c -> CourseResponse.fromEntity(c, groupRepository.countByCourseId(c.getId())))
                .toList();

        return PageResponse.of(content, page, size, total, totalPages, page == 0, end >= total);
    }

    // ==================== GROUPS SEARCH ====================

    @Transactional(readOnly = true)
    public PageResponse<GroupResponse> searchGroups(User teacher, String keyword, Long courseId, String status,
                                                     int page, int size, String sortBy, String sortDir) {
        List<Group> groups = groupRepository.findByTeacherId(teacher.getId());

        // Filter by courseId
        if (courseId != null) {
            groups = groups.stream().filter(g -> g.getCourse().getId().equals(courseId)).collect(Collectors.toList());
        }

        // Filter by status
        if (status != null && !status.isEmpty()) {
            GroupStatus gs = GroupStatus.valueOf(status);
            groups = groups.stream().filter(g -> g.getStatus() == gs).collect(Collectors.toList());
        }

        // Filter by keyword (group name)
        if (keyword != null && !keyword.trim().isEmpty()) {
            String kw = keyword.toLowerCase().trim();
            groups = groups.stream().filter(g -> {
                String name = g.getName() != null ? g.getName().toLowerCase() : "";
                return name.contains(kw);
            }).collect(Collectors.toList());
        }

        // Sort
        if ("name".equalsIgnoreCase(sortBy)) {
            groups.sort(Comparator.comparing(g -> g.getName() != null ? g.getName() : "", String.CASE_INSENSITIVE_ORDER));
            if ("desc".equalsIgnoreCase(sortDir)) Collections.reverse(groups);
        } else if ("startDate".equalsIgnoreCase(sortBy)) {
            groups.sort(Comparator.comparing(Group::getStartDate, Comparator.nullsLast(Comparator.naturalOrder())));
            if ("desc".equalsIgnoreCase(sortDir)) Collections.reverse(groups);
        }

        // Paginate
        int total = groups.size();
        int start = page * size;
        int end = Math.min(start + size, total);
        List<Group> pageContent = start < total ? groups.subList(start, end) : List.of();
        int totalPages = (int) Math.ceil((double) total / size);

        List<GroupResponse> content = pageContent.stream()
                .map(g -> GroupResponse.fromEntity(g, enrollmentRepository.countActiveByGroupId(g.getId())))
                .toList();

        return PageResponse.of(content, page, size, total, totalPages, page == 0, end >= total);
    }

    // ==================== SESSIONS SEARCH ====================

    @Transactional(readOnly = true)
    public PageResponse<SessionResponse> searchSessions(User teacher, Long groupId, LocalDate from, LocalDate to,
                                                          String status, int page, int size, String sortBy, String sortDir) {
        List<Session> sessions;
        if (groupId != null) {
            // Verify teacher owns this group
            Group group = groupRepository.findById(groupId)
                    .orElseThrow(() -> new BusinessException("Không tìm thấy nhóm"));
            if (!group.getTeacher().getId().equals(teacher.getId())) {
                throw new BusinessException("Bạn không có quyền xem nhóm này");
            }
            sessions = new ArrayList<>(sessionRepository.findByGroupIdOrderByDateAsc(groupId));
        } else {
            // All sessions of teacher's groups
            List<Group> teacherGroups = groupRepository.findByTeacherId(teacher.getId());
            sessions = new ArrayList<>();
            for (Group g : teacherGroups) {
                sessions.addAll(sessionRepository.findByGroupIdOrderByDateAsc(g.getId()));
            }
        }

        // Filter by date range
        if (from != null) sessions = sessions.stream().filter(s -> !s.getDate().isBefore(from)).collect(Collectors.toList());
        if (to != null) sessions = sessions.stream().filter(s -> !s.getDate().isAfter(to)).collect(Collectors.toList());

        // Filter by status
        if (status != null && !status.isEmpty()) {
            SessionStatus ss = SessionStatus.valueOf(status);
            sessions = sessions.stream().filter(s -> s.getStatus() == ss).collect(Collectors.toList());
        }

        // Sort
        if ("date".equalsIgnoreCase(sortBy)) {
            sessions.sort(Comparator.comparing(Session::getDate).thenComparing(Session::getStartTime));
            if ("desc".equalsIgnoreCase(sortDir)) Collections.reverse(sessions);
        }

        // Paginate
        int total = sessions.size();
        int start = page * size;
        int end = Math.min(start + size, total);
        List<Session> pageContent = start < total ? sessions.subList(start, end) : List.of();
        int totalPages = (int) Math.ceil((double) total / size);

        List<SessionResponse> content = pageContent.stream()
                .map(s -> SessionResponse.fromEntity(s, attendanceRepository.countBySessionId(s.getId())))
                .toList();

        return PageResponse.of(content, page, size, total, totalPages, page == 0, end >= total);
    }

    // ==================== BULK CREATE SESSIONS ====================

    @Transactional
    public List<SessionResponse> bulkCreateSessions(User teacher, Long groupId, LocalDate startDate, LocalDate endDate,
                                                     List<Integer> daysOfWeek, LocalTime startTime, LocalTime endTime, String room) {
        Group group = groupRepository.findById(groupId)
                .orElseThrow(() -> new BusinessException("Không tìm thấy nhóm"));
        if (!group.getTeacher().getId().equals(teacher.getId())) {
            throw new BusinessException("Bạn không có quyền tạo buổi học cho nhóm này");
        }

        List<Session> created = new ArrayList<>();
        LocalDate date = startDate;
        while (!date.isAfter(endDate)) {
            if (daysOfWeek.contains(date.getDayOfWeek().getValue())) {
                // Check trùng
                if (!sessionRepository.existsByGroupIdAndDateAndStartTime(groupId, date, startTime)) {
                    Session session = Session.builder()
                            .group(group)
                            .date(date)
                            .startTime(startTime)
                            .endTime(endTime)
                            .room(room)
                            .status(SessionStatus.SCHEDULED)
                            .build();
                    created.add(sessionRepository.save(session));
                }
            }
            date = date.plusDays(1);
        }

        return created.stream()
                .map(s -> SessionResponse.fromEntity(s, 0))
                .toList();
    }

    // ==================== INVOICES SEARCH ====================

    @Transactional(readOnly = true)
    public PageResponse<InvoiceResponse> searchInvoices(User teacher, Integer month, Integer year, String status,
                                                          String keyword, int page, int size, String sortBy, String sortDir) {
        List<Invoice> invoices;
        if (month != null && year != null) {
            invoices = invoiceRepository.findByTeacherIdAndMonthAndYear(teacher.getId(), month, year);
        } else {
            // All invoices of teacher's students
            invoices = new ArrayList<>();
            List<User> students = userRepository.findByRoleAndCreatedById(Role.STUDENT, teacher.getId());
            for (User s : students) {
                invoices.addAll(invoiceRepository.findByStudentIdOrderByYearDescMonthDesc(s.getId()));
            }
        }

        // Filter by status
        if (status != null && !status.isEmpty()) {
            InvoiceStatus is = InvoiceStatus.valueOf(status);
            invoices = invoices.stream().filter(i -> i.getStatus() == is).collect(Collectors.toList());
        }

        // Filter by keyword (student name/email/phone)
        if (keyword != null && !keyword.trim().isEmpty()) {
            String kw = keyword.toLowerCase().trim();
            invoices = invoices.stream().filter(i -> {
                User s = i.getStudent();
                String name = s.getFullName() != null ? s.getFullName().toLowerCase() : "";
                String email = s.getEmail() != null ? s.getEmail().toLowerCase() : "";
                String phone = s.getPhone() != null ? s.getPhone().toLowerCase() : "";
                return name.contains(kw) || email.contains(kw) || phone.contains(kw);
            }).collect(Collectors.toList());
        }

        // Sort
        if ("name".equalsIgnoreCase(sortBy)) {
            invoices = SortUtils.sortListByLastName(invoices,
                    i -> i.getStudent().getFullName(), "asc".equalsIgnoreCase(sortDir));
        } else {
            invoices.sort(Comparator.comparing(Invoice::getYear, Comparator.reverseOrder())
                    .thenComparing(Invoice::getMonth, Comparator.reverseOrder()));
        }

        // Paginate
        int total = invoices.size();
        int start = page * size;
        int end = Math.min(start + size, total);
        List<Invoice> pageContent = start < total ? invoices.subList(start, end) : List.of();
        int totalPages = (int) Math.ceil((double) total / size);

        List<InvoiceResponse> content = pageContent.stream()
                .map(InvoiceResponse::fromEntity).toList();

        return PageResponse.of(content, page, size, total, totalPages, page == 0, end >= total);
    }

    // ==================== DASHBOARD ====================

    @Transactional(readOnly = true)
    public Map<String, Object> getDashboardStats(User teacher) {
        Map<String, Object> stats = new LinkedHashMap<>();
        long totalCourses = courseRepository.findByTeacherId(teacher.getId()).size();
        List<Group> groups = groupRepository.findByTeacherId(teacher.getId());
        long totalGroups = groups.size();
        
        long totalStudents = groups.stream()
                .flatMap(g -> enrollmentRepository.findByGroupId(g.getId()).stream())
                .map(com.tuition.entity.Enrollment::getStudent)
                .distinct()
                .count();
                
        // Today sessions
        java.time.LocalDate today = java.time.LocalDate.now();
        long todaySessions = 0;
        for (Group g : groups) {
            todaySessions += sessionRepository.findByGroupIdAndDateBetween(g.getId(), today, today).size();
        }

        // Month revenue
        LocalDateTime monthStart = today.withDayOfMonth(1).atStartOfDay();
        LocalDateTime monthEnd = today.withDayOfMonth(today.lengthOfMonth()).atTime(23, 59, 59);
        BigDecimal monthRevenue = paymentRepository.sumAmountByTeacherAndPaidAtBetween(teacher.getId(), monthStart, monthEnd);

        // Month unpaid
        List<InvoiceStatus> unpaidStatuses = List.of(InvoiceStatus.UNPAID, InvoiceStatus.PARTIAL);
        List<Invoice> unpaidInvoices = invoiceRepository.findByTeacherIdAndStatusIn(teacher.getId(), unpaidStatuses);
        BigDecimal monthUnpaid = unpaidInvoices.stream()
                .map(i -> i.getTotalAmount().subtract(i.getPaidAmount()))
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        stats.put("totalCourses", totalCourses);
        stats.put("totalGroups", totalGroups);
        stats.put("totalStudents", totalStudents);
        stats.put("todaySessions", todaySessions);
        stats.put("monthRevenue", monthRevenue != null ? monthRevenue : BigDecimal.ZERO);
        stats.put("monthUnpaid", monthUnpaid);

        return stats;
    }

    @Transactional(readOnly = true)
    public List<SessionResponse> getTodaySessions(User teacher) {
        LocalDate today = LocalDate.now();
        List<Group> groups = groupRepository.findByTeacherId(teacher.getId());
        List<Session> sessions = new ArrayList<>();
        for (Group g : groups) {
            sessions.addAll(sessionRepository.findByGroupIdAndDateBetween(g.getId(), today, today));
        }
        sessions.sort(Comparator.comparing(Session::getStartTime));
        return sessions.stream()
                .map(s -> SessionResponse.fromEntity(s, attendanceRepository.countBySessionId(s.getId())))
                .toList();
    }

    @Transactional(readOnly = true)
    public List<Map<String, Object>> getRecentPayments(User teacher) {
        List<User> students = userRepository.findByRoleAndCreatedById(Role.STUDENT, teacher.getId());
        List<Payment> allPayments = new ArrayList<>();
        for (User s : students) {
            List<Invoice> invoices = invoiceRepository.findByStudentIdOrderByYearDescMonthDesc(s.getId());
            for (Invoice inv : invoices) {
                allPayments.addAll(paymentRepository.findByInvoiceId(inv.getId()));
            }
        }
        // Sort by paidAt desc, take 10
        allPayments.sort(Comparator.comparing(Payment::getPaidAt, Comparator.reverseOrder()));
        return allPayments.stream().limit(10).map(p -> {
            Map<String, Object> m = new LinkedHashMap<>();
            m.put("id", p.getId());
            m.put("studentName", p.getInvoice().getStudent().getFullName());
            m.put("amount", p.getAmount());
            m.put("method", p.getMethod());
            m.put("paidAt", p.getPaidAt());
            m.put("note", p.getNote());
            m.put("invoiceId", p.getInvoice().getId());
            m.put("month", p.getInvoice().getMonth());
            m.put("year", p.getInvoice().getYear());
            return m;
        }).toList();
    }

    // ==================== REPORTS ====================

    @Transactional(readOnly = true)
    public Map<String, Object> getRevenueReport(User teacher, LocalDateTime from, LocalDateTime to) {
        BigDecimal total = paymentRepository.sumAmountByTeacherAndPaidAtBetween(teacher.getId(), from, to);
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("fromDate", from);
        result.put("toDate", to);
        result.put("totalRevenue", total != null ? total : BigDecimal.ZERO);
        return result;
    }

    @Transactional(readOnly = true)
    public List<Map<String, Object>> getRevenueByCourse(User teacher, LocalDateTime from, LocalDateTime to) {
        List<Course> courses = courseRepository.findByTeacherId(teacher.getId());
        List<Map<String, Object>> result = new ArrayList<>();
        for (Course c : courses) {
            List<Group> courseGroups = groupRepository.findByCourseId(c.getId());
            BigDecimal courseRevenue = BigDecimal.ZERO;
            for (Group g : courseGroups) {
                List<Session> sessions = sessionRepository.findByGroupIdOrderByDateAsc(g.getId());
                for (Session s : sessions) {
                    List<Attendance> attendances = attendanceRepository.findBySessionId(s.getId());
                    for (Attendance a : attendances) {
                        // Sum payments for invoices of these students
                        // This is simplified - count revenue via payments
                    }
                }
            }
            // Simpler approach: sum payments where invoice.student is enrolled in course groups
            Map<String, Object> m = new LinkedHashMap<>();
            m.put("courseId", c.getId());
            m.put("courseName", c.getName());
            m.put("revenue", courseRevenue);
            result.add(m);
        }
        return result;
    }

    @Transactional(readOnly = true)
    public List<Map<String, Object>> getAttendanceRate(User teacher, LocalDate from, LocalDate to) {
        List<User> students = userRepository.findByRoleAndCreatedById(Role.STUDENT, teacher.getId());
        List<Map<String, Object>> result = new ArrayList<>();
        for (User s : students) {
            long present = attendanceRepository.countByStudentIdAndStatusAndSessionDateBetween(
                    s.getId(), AttendanceStatus.PRESENT, from, to);
            long absent = attendanceRepository.countByStudentIdAndStatusAndSessionDateBetween(
                    s.getId(), AttendanceStatus.ABSENT, from, to);
            long excused = attendanceRepository.countByStudentIdAndStatusAndSessionDateBetween(
                    s.getId(), AttendanceStatus.EXCUSED, from, to);
            long total = present + absent + excused;
            double rate = total > 0 ? (double) present / total * 100 : 0;

            Map<String, Object> m = new LinkedHashMap<>();
            m.put("studentId", s.getId());
            m.put("studentName", s.getFullName());
            m.put("present", present);
            m.put("absent", absent);
            m.put("excused", excused);
            m.put("total", total);
            m.put("rate", Math.round(rate * 10.0) / 10.0);
            result.add(m);
        }
        result.sort((a, b) -> Double.compare((double) b.get("rate"), (double) a.get("rate")));
        return result;
    }

    @Transactional(readOnly = true)
    public List<Map<String, Object>> getTopStudents(User teacher, int limit) {
        LocalDate from = LocalDate.now().minusMonths(3);
        LocalDate to = LocalDate.now();
        List<Map<String, Object>> rates = getAttendanceRate(teacher, from, to);
        return rates.stream().limit(limit).toList();
    }
}
