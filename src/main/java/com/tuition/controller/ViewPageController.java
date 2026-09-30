package com.tuition.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

/**
 * Controller phục vụ tất cả trang Thymeleaf (Single Page View).
 * Logic nghiệp vụ nằm ở REST API, JS sẽ gọi API.
 */
@Controller
public class ViewPageController {

    // ==================== ADMIN ====================
    @GetMapping("/admin/users")
    public String adminUsers() { return "admin/users"; }

    @GetMapping("/admin/courses")
    public String adminCourses() { return "admin/courses"; }

    @GetMapping("/admin/course-form")
    public String adminCourseForm() { return "admin/course-form"; }

    @GetMapping("/admin/groups")
    public String adminGroups() { return "admin/groups"; }

    @GetMapping("/admin/group-form")
    public String adminGroupForm() { return "admin/group-form"; }

    @GetMapping("/admin/group-detail")
    public String adminGroupDetail() { return "admin/group-detail"; }

    @GetMapping("/admin/sessions")
    public String adminSessions() { return "admin/sessions"; }

    @GetMapping("/admin/session-form")
    public String adminSessionForm() { return "admin/session-form"; }

    @GetMapping("/admin/attendance")
    public String adminAttendance() { return "admin/attendance"; }

    @GetMapping("/admin/invoices")
    public String adminInvoices() { return "admin/invoices"; }

    @GetMapping("/admin/invoice-detail")
    public String adminInvoiceDetail() { return "admin/invoice-detail"; }

    @GetMapping("/admin/reports")
    public String adminReports() { return "admin/reports"; }

    // ==================== TEACHER ====================
    @GetMapping("/teacher/courses")
    public String teacherCourses() { return "teacher/courses"; }

    @GetMapping("/teacher/course-form")
    public String teacherCourseForm() { return "teacher/course-form"; }

    @GetMapping("/teacher/groups")
    public String teacherGroups() { return "teacher/groups"; }

    @GetMapping("/teacher/group-form")
    public String teacherGroupForm() { return "teacher/group-form"; }

    @GetMapping("/teacher/group-detail")
    public String teacherGroupDetail() { return "teacher/group-detail"; }

    @GetMapping("/teacher/students")
    public String teacherStudents() { return "teacher/students"; }

    @GetMapping("/teacher/sessions")
    public String teacherSessions() { return "teacher/sessions"; }

    @GetMapping("/teacher/session-form")
    public String teacherSessionForm() { return "teacher/session-form"; }

    @GetMapping("/teacher/attendance")
    public String teacherAttendance() { return "teacher/attendance"; }

    @GetMapping("/teacher/invoices")
    public String teacherInvoices() { return "teacher/invoices"; }

    @GetMapping("/teacher/invoice-detail")
    public String teacherInvoiceDetail() { return "teacher/invoice-detail"; }

    @GetMapping("/teacher/reports")
    public String teacherReports() { return "teacher/reports"; }

    // ==================== STUDENT ====================
    @GetMapping("/student/schedule")
    public String studentSchedule() { return "student/schedule"; }

    @GetMapping("/student/attendance")
    public String studentAttendance() { return "student/attendance"; }

    @GetMapping("/student/invoices")
    public String studentInvoices() { return "student/invoices"; }

    @GetMapping("/student/invoice-detail")
    public String studentInvoiceDetail() { return "student/invoice-detail"; }
}
