package com.example.auth.controller;

import com.example.auth.dto.response.UserResponse;
import com.example.auth.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    /** Get the profile of the currently authenticated user (derived from JWT). */
    @GetMapping("/me")
    public UserResponse getMe(Authentication authentication) {
        UUID userId = (UUID) authentication.getPrincipal();
        return userService.findById(userId);
    }

    /**
     * Get a user by ID.
     * This endpoint is the cross-service bridge: the task-service calls it
     * to validate that an assigneeUserId refers to a real, active user.
     */
    @GetMapping("/{id}")
    public UserResponse getUserById(@PathVariable UUID id) {
        return userService.findById(id);
    }

    /** List all registered users — ADMIN only. */
    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public List<UserResponse> getAllUsers() {
        return userService.findAll();
    }
}

