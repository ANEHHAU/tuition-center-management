package com.tuition.controller;

import com.tuition.dto.BaseSearchRequest;
import com.tuition.dto.PageResponse;
import com.tuition.dto.SessionRequest;
import com.tuition.dto.SessionResponse;
import com.tuition.entity.User;
import com.tuition.repository.UserRepository;
import com.tuition.service.SessionService;
import com.tuition.service.TeacherService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/teacher/sessions")
@RequiredArgsConstructor
@PreAuthorize("hasRole('TEACHER')")
public class TeacherSessionController {

    private final SessionService sessionService;
    private final TeacherService teacherService;
    private final UserRepository userRepository;

    private User getCurrentUser(UserDetails userDetails) {
        return userRepository.findByUsername(userDetails.getUsername()).orElseThrow();
    }

    @GetMapping
    public PageResponse<SessionResponse> getSessions(
            @ModelAttribute BaseSearchRequest req,
            @RequestParam(required = false) Long groupId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to,
            @RequestParam(required = false) String status,
            @AuthenticationPrincipal UserDetails userDetails) {
        return teacherService.searchSessions(getCurrentUser(userDetails), groupId, from, to, status, req.getPage(), req.getSize(), req.getSortBy(), req.getSortDir());
    }

    @PostMapping("/bulk-create")
    @ResponseStatus(HttpStatus.CREATED)
    public List<SessionResponse> bulkCreate(
            @RequestBody Map<String, Object> payload,
            @AuthenticationPrincipal UserDetails userDetails) {
        Long groupId = ((Number) payload.get("groupId")).longValue();
        LocalDate startDate = LocalDate.parse((String) payload.get("startDate"));
        LocalDate endDate = LocalDate.parse((String) payload.get("endDate"));
        List<Integer> daysOfWeek = ((List<?>) payload.get("daysOfWeek")).stream().map(o -> ((Number) o).intValue()).toList();
        LocalTime startTime = LocalTime.parse((String) payload.get("startTime"));
        LocalTime endTime = LocalTime.parse((String) payload.get("endTime"));
        String room = (String) payload.get("room");

        return teacherService.bulkCreateSessions(getCurrentUser(userDetails), groupId, startDate, endDate, daysOfWeek, startTime, endTime, room);
    }

    @GetMapping("/{id}")
    public SessionResponse getSession(@PathVariable Long id, @AuthenticationPrincipal UserDetails userDetails) {
        return sessionService.getById(id, getCurrentUser(userDetails));
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public SessionResponse createSession(@RequestBody @Valid SessionRequest req, @AuthenticationPrincipal UserDetails userDetails) {
        return sessionService.create(req, getCurrentUser(userDetails));
    }

    @PutMapping("/{id}")
    public SessionResponse updateSession(@PathVariable Long id, @RequestBody @Valid SessionRequest req, @AuthenticationPrincipal UserDetails userDetails) {
        return sessionService.update(id, req, getCurrentUser(userDetails));
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteSession(@PathVariable Long id, @AuthenticationPrincipal UserDetails userDetails) {
        sessionService.delete(id, getCurrentUser(userDetails));
    }

    @PostMapping("/{id}/cancel")
    public SessionResponse cancelSession(@PathVariable Long id, @AuthenticationPrincipal UserDetails userDetails) {
        return sessionService.cancel(id, getCurrentUser(userDetails));
    }
}
