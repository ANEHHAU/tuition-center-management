package com.tuition.controller;

import com.tuition.entity.Group;
import com.tuition.entity.PublicLinkAccess;
import com.tuition.repository.PublicLinkAccessRepository;
import com.tuition.service.PublicLinkService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.List;
import java.util.Map;

/**
 * Controller phục vụ xem LỊCH HỌC công khai cho phụ huynh/học sinh không cần đăng nhập.
 */
@Controller
@RequestMapping("/public")
@RequiredArgsConstructor
@Slf4j
public class PublicScheduleController {

    private final PublicLinkService publicLinkService;
    private final PublicLinkAccessRepository publicLinkAccessRepository;

    @GetMapping("/schedule/{token}")
    public String viewPublicSchedule(
            @PathVariable String token,
            HttpServletRequest request,
            HttpServletResponse response,
            Model model
    ) {
        // 1. Kiểm tra tính hợp lệ của Token
        Group group = publicLinkService.validateToken(token);

        // 2. Ghi vết truy cập (Access Log) vào DB
        try {
            String ip = request.getHeader("X-Forwarded-For");
            if (ip == null || ip.isEmpty()) {
                ip = request.getRemoteAddr();
            }
            String userAgent = request.getHeader("User-Agent");

            PublicLinkAccess access = PublicLinkAccess.builder()
                    .publicToken(token)
                    .ipAddress(ip)
                    .userAgent(userAgent != null ? (userAgent.length() > 250 ? userAgent.substring(0, 250) : userAgent) : "Unknown")
                    .build();

            publicLinkAccessRepository.save(access);
        } catch (Exception e) {
            log.warn("Không thể ghi vết truy cập public link: {}", e.getMessage());
        }

        // 3. Thiết lập các HTTP Headers bảo mật chống index & ẩn Referrer
        response.setHeader("Referrer-Policy", "no-referrer");
        response.setHeader("X-Robots-Tag", "noindex, nofollow");

        // 4. Chuẩn bị dữ liệu hiển thị (CHỈ BAO GỒM LỊCH HỌC - KHÔNG CÓ GIÁ TIỀN hay DS HỌC SINH)
        model.addAttribute("groupName", group.getName());
        model.addAttribute("courseName", group.getCourse().getName());

        // Danh sách buổi học mẫu cho nhóm
        List<Map<String, String>> sessions = List.of(
                Map.of("date", "Thứ Hai, 06/10/2026", "startTime", "18:00", "endTime", "20:00", "room", "Phòng A101", "status", "Sắp diễn ra"),
                Map.of("date", "Thứ Tư, 08/10/2026", "startTime", "18:00", "endTime", "20:00", "room", "Phòng A101", "status", "Sắp diễn ra"),
                Map.of("date", "Thứ Sáu, 10/10/2026", "startTime", "18:00", "endTime", "20:00", "room", "Phòng A101", "status", "Sắp diễn ra")
        );
        model.addAttribute("sessions", sessions);

        return "public/schedule-view";
    }
}
