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
        return "redirect:/admin/users";
    }

    @GetMapping("/student")
    public String studentHome() {
        return "student/home";
    }

    // Điều hướng trang chủ mặc định về trang đăng nhập
    @GetMapping("/")
    public String index() {
        return "redirect:/login";
    }
}
