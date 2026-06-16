package com.example.auth.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LoginResponse {

    private String token;
    /** Expiry duration in milliseconds (matches JWT exp claim) */
    private long expiresIn;
    private UserResponse user;
}

