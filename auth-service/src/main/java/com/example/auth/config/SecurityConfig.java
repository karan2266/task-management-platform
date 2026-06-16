package com.example.auth.config;

import com.example.auth.dto.response.ErrorResponse;
import com.example.auth.security.JwtAuthenticationFilter;
import com.example.auth.security.JwtTokenProvider;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import java.time.Instant;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final JwtTokenProvider jwtTokenProvider;
    private final ObjectMapper objectMapper;

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        /*
         * JwtAuthenticationFilter is instantiated here (not as a @Bean / @Component)
         * to prevent Spring Boot from auto-registering it as a servlet filter,
         * which would cause it to execute twice per request.
         */
        JwtAuthenticationFilter jwtFilter = new JwtAuthenticationFilter(jwtTokenProvider);

        http
            .csrf(csrf -> csrf.disable())
            .sessionManagement(sm ->
                sm.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            .authorizeHttpRequests(auth -> auth
                .requestMatchers("/api/v1/auth/**").permitAll()
                .anyRequest().authenticated()
            )
            .exceptionHandling(exc -> exc
                // 401 — token missing / invalid
                .authenticationEntryPoint((req, res, ex) -> {
                    res.setContentType(MediaType.APPLICATION_JSON_VALUE);
                    res.setStatus(HttpStatus.UNAUTHORIZED.value());
                    objectMapper.writeValue(res.getOutputStream(), ErrorResponse.builder()
                            .timestamp(Instant.now())
                            .status(HttpStatus.UNAUTHORIZED.value())
                            .message("Authentication required")
                            .path(req.getRequestURI())
                            .build());
                })
                // 403 — authenticated but insufficient privilege
                .accessDeniedHandler((req, res, ex) -> {
                    res.setContentType(MediaType.APPLICATION_JSON_VALUE);
                    res.setStatus(HttpStatus.FORBIDDEN.value());
                    objectMapper.writeValue(res.getOutputStream(), ErrorResponse.builder()
                            .timestamp(Instant.now())
                            .status(HttpStatus.FORBIDDEN.value())
                            .message("Access denied")
                            .path(req.getRequestURI())
                            .build());
                })
            )
            .addFilterBefore(jwtFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }
}

