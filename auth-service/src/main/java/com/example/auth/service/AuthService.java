package com.example.auth.service;

import com.example.auth.dto.request.LoginRequest;
import com.example.auth.dto.request.RegisterRequest;
import com.example.auth.dto.response.LoginResponse;
import com.example.auth.dto.response.TokenRefreshResponse;
import com.example.auth.dto.response.UserResponse;
import com.example.auth.dto.request.RefreshTokenRequest;
import com.example.auth.entity.RefreshToken;
import com.example.auth.entity.User;
import com.example.auth.enums.Role;
import com.example.auth.exception.EmailAlreadyExistsException;
import com.example.auth.repository.UserRepository;
import com.example.auth.security.JwtTokenProvider;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.security.authentication.DisabledException;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider jwtTokenProvider;
    private final RefreshTokenService refreshTokenService;

    @Value("${app.jwt.expiration-ms}")
    private long jwtExpirationMs;

    @Transactional
    public UserResponse register(RegisterRequest request) {
        String normalizedEmail = request.getEmail().toLowerCase().strip();

        if (userRepository.existsByEmail(normalizedEmail)) {
            throw new EmailAlreadyExistsException(normalizedEmail);
        }

        User user = User.builder()
                .email(normalizedEmail)
                .passwordHash(passwordEncoder.encode(request.getPassword()))
                .fullName(request.getFullName().strip())
                .role(Role.USER)
                .active(true)
                .build();

        return toResponse(userRepository.save(user));
    }

    @Transactional
    public LoginResponse login(LoginRequest request) {
        String normalizedEmail = request.getEmail().toLowerCase().strip();

        User user = userRepository.findByEmail(normalizedEmail)
                .orElseThrow(() -> new BadCredentialsException("Invalid email or password"));

        if (!user.isActive()) {
            throw new DisabledException("Account is deactivated");
        }

        if (!passwordEncoder.matches(request.getPassword(), user.getPasswordHash())) {
            throw new BadCredentialsException("Invalid email or password");
        }

        RefreshToken refreshToken = refreshTokenService.create(user.getId());

        return LoginResponse.builder()
                .token(jwtTokenProvider.generateToken(user))
                .refreshToken(refreshToken.getToken())
                .expiresIn(jwtExpirationMs)
                .user(toResponse(user))
                .build();
    }

    @Transactional
    public TokenRefreshResponse refresh(RefreshTokenRequest request) {
        RefreshToken validToken = refreshTokenService.verify(request.getRefreshToken());
        User user = validToken.getUser();

        // Rotate token
        RefreshToken newRefreshToken = refreshTokenService.create(user.getId());

        return TokenRefreshResponse.builder()
                .accessToken(jwtTokenProvider.generateToken(user))
                .refreshToken(newRefreshToken.getToken())
                .expiresIn(jwtExpirationMs)
                .build();
    }

    // ── helpers ──────────────────────────────────────────────────────────────

    UserResponse toResponse(User user) {
        return UserResponse.builder()
                .id(user.getId())
                .email(user.getEmail())
                .fullName(user.getFullName())
                .role(user.getRole())
                .createdAt(user.getCreatedAt())
                .active(user.isActive())
                .build();
    }
}

