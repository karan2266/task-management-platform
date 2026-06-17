package com.example.auth.controller;

import com.example.auth.dto.request.LoginRequest;
import com.example.auth.dto.request.RegisterRequest;
import com.example.auth.dto.response.LoginResponse;
import com.example.auth.dto.response.UserResponse;
import com.example.auth.service.AuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import com.example.auth.dto.response.TokenRefreshResponse;
import com.example.auth.dto.request.RefreshTokenRequest;

@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    /** Register a new user account — returns the created user profile (no password). */
    @PostMapping("/register")
    @ResponseStatus(HttpStatus.CREATED)
    public UserResponse register(@Valid @RequestBody RegisterRequest request) {
        return authService.register(request);
    }

    /** Authenticate and receive a signed JWT. */
    @PostMapping("/login")
    public LoginResponse login(@Valid @RequestBody LoginRequest request) {
        return authService.login(request);
    }

    /** Use a refresh token to get a new access token. */
    @PostMapping("/refresh")
    public TokenRefreshResponse refresh(@Valid @RequestBody RefreshTokenRequest request) {
        return authService.refresh(request);
    }
}

