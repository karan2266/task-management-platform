package com.example.auth.config;

import com.example.auth.entity.User;
import com.example.auth.enums.Role;
import com.example.auth.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class DataInitializer implements CommandLineRunner {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Value("${app.admin.email}")
    private String adminEmail;

    @Value("${app.admin.password}")
    private String adminPassword;

    @Value("${app.admin.full-name}")
    private String adminFullName;

    @Override
    public void run(String... args) {
        String normalizedEmail = adminEmail.toLowerCase().strip();

        if (userRepository.existsByEmail(normalizedEmail)) {
            log.info("DataInitializer: admin user [{}] already exists — skipping.", normalizedEmail);
            return;
        }

        User admin = User.builder()
                .email(normalizedEmail)
                .passwordHash(passwordEncoder.encode(adminPassword))
                .fullName(adminFullName)
                .role(Role.ADMIN)
                .active(true)
                .build();

        userRepository.save(admin);
        log.info("DataInitializer: default admin user created → email: {}", normalizedEmail);
    }
}

