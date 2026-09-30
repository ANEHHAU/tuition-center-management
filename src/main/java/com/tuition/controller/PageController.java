package com.tuition.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

/**
 * Controller điều hướng trả về các giao diện Thymeleaf HTML.
 */
@Controller
public class PageController {

    // Giao diện công khai
    @GetMapping("/login")
    public String loginPage() {
        return "auth/login";
    }

    @GetMapping("/register")
    public String registerPage() {
        return "auth/register";
    }

    // Giao diện người dùng sau khi đăng nhập
    @GetMapping("/profile")
    public String profilePage() {
        return "auth/profile";
    }

    @GetMapping("/admin")
    public String adminHome() {
        return "admin/home";
    }

    @GetMapping("/student")
    public String studentHome() {
        return "redirect:/student/dashboard";
    }

    @GetMapping("/student/dashboard")
    public String studentDashboard() { return "student/dashboard"; }

    @GetMapping("/student/schedule")
    public String studentSchedule() { return "student/schedule"; }
    
    @GetMapping("/student/session-detail")
    public String studentSessionDetail() { return "student/session-detail"; }

    @GetMapping("/student/attendance")
    public String studentAttendance() { return "student/attendance"; }

    @GetMapping("/student/courses")
    public String studentCourses() { return "student/courses"; }

    @GetMapping("/student/invoices")
    public String studentInvoices() { return "student/invoices"; }

    @GetMapping("/student/invoice-detail")
    public String studentInvoiceDetail() { return "student/invoice-detail"; }

    // Điều hướng trang chủ mặc định về trang đăng nhập
    @GetMapping("/")
    public String index() {
        return "redirect:/login";
    }
}
