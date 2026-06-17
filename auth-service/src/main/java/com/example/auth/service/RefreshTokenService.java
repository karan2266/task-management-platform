package com.example.auth.service;

import com.example.auth.entity.RefreshToken;
import com.example.auth.exception.TokenRefreshException;
import com.example.auth.repository.RefreshTokenRepository;
import com.example.auth.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class RefreshTokenService {

    private final RefreshTokenRepository refreshTokenRepository;
    private final UserRepository userRepository;

    @Transactional
    public RefreshToken create(UUID userId) {
        revokeByUser(userId);

        RefreshToken token = RefreshToken.builder()
                .user(userRepository.getReferenceById(userId))
                .token(UUID.randomUUID().toString())
                .expiryDate(LocalDateTime.now().plusDays(7))
                .revoked(false)
                .build();
        return refreshTokenRepository.save(token);
    }

    @Transactional(readOnly = true)
    public RefreshToken verify(String token) {
        RefreshToken refreshToken = refreshTokenRepository.findByToken(token)
                .orElseThrow(() -> new TokenRefreshException("Invalid refresh token"));

        if (refreshToken.isRevoked()) {
            throw new TokenRefreshException("Refresh token was revoked");
        }
        if (refreshToken.getExpiryDate().isBefore(LocalDateTime.now())) {
            throw new TokenRefreshException("Refresh token expired");
        }
        return refreshToken;
    }

    @Transactional
    public void revokeByUser(UUID userId) {
        refreshTokenRepository.revokeAllUserTokens(userId);
    }
}
