package com.tuition.controller;

import com.tuition.dto.SessionRequest;
import com.tuition.dto.SessionResponse;
import com.tuition.entity.User;
import com.tuition.repository.UserRepository;
import com.tuition.service.SessionService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

/**
 * Controller quản lý buổi học cho Admin
 */
@RestController
@RequestMapping("/api/admin/sessions")
@RequiredArgsConstructor
public class AdminSessionController {

    private final SessionService sessionService;
    private final UserRepository userRepository;

    private User getCurrentUser(UserDetails userDetails) {
        return userRepository.findByUsername(userDetails.getUsername()).orElseThrow();
    }

    @GetMapping
    public List<SessionResponse> getSessions(
            @RequestParam(required = false) Long groupId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to,
            @AuthenticationPrincipal UserDetails userDetails) {
        User currentUser = getCurrentUser(userDetails);
        if (groupId == null) {
            return sessionService.listAll(currentUser, from, to);
        }
        if (from != null && to != null) {
            return sessionService.listByRange(groupId, from, to, currentUser);
        }
        return sessionService.listByGroup(groupId, currentUser);
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
