package com.tuition.controller;

import com.tuition.dto.GroupRequest;
import com.tuition.dto.GroupResponse;
import com.tuition.entity.User;
import com.tuition.repository.UserRepository;
import com.tuition.service.GroupService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/admin/groups")
@RequiredArgsConstructor
public class AdminGroupController {

    private final GroupService groupService;
    private final UserRepository userRepository;

    private User getCurrentUser(UserDetails userDetails) {
        return userRepository.findByUsername(userDetails.getUsername()).orElseThrow();
    }

    @GetMapping
    public List<GroupResponse> getAllGroups(@AuthenticationPrincipal UserDetails userDetails) {
        return groupService.listByCurrentTeacher(getCurrentUser(userDetails));
    }

    @GetMapping("/{id}")
    public GroupResponse getGroup(@PathVariable Long id, @AuthenticationPrincipal UserDetails userDetails) {
        return groupService.getById(id, getCurrentUser(userDetails));
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public GroupResponse createGroup(@RequestBody @Valid GroupRequest req, @AuthenticationPrincipal UserDetails userDetails) {
        return groupService.create(req, getCurrentUser(userDetails));
    }

    @PutMapping("/{id}")
    public GroupResponse updateGroup(@PathVariable Long id, @RequestBody @Valid GroupRequest req, @AuthenticationPrincipal UserDetails userDetails) {
        return groupService.update(id, req, getCurrentUser(userDetails));
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteGroup(@PathVariable Long id, @AuthenticationPrincipal UserDetails userDetails) {
        groupService.delete(id, getCurrentUser(userDetails));
    }
}
