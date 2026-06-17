package com.example.task.client;

import com.example.task.dto.response.UserResponse;
import com.example.task.exception.ServiceUnavailableException;
import com.example.task.exception.UserNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestTemplate;

import java.util.UUID;

/**
 * Calls auth-service GET /api/v1/users/{id} to validate that an assignee
 * exists and is active before a task assignment is persisted.
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class UserValidationClient {

    private final RestTemplate restTemplate;

    @Value("${app.auth-service.url}")
    private String authServiceUrl;

    public UserResponse getUser(UUID userId, String bearerToken) {
        String url = authServiceUrl + "/api/v1/users/" + userId;

        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(bearerToken);
        HttpEntity<?> entity = new HttpEntity<>(headers);

        try {
            ResponseEntity<UserResponse> response = restTemplate.exchange(
                    url, HttpMethod.GET, entity, UserResponse.class);

            UserResponse user = response.getBody();

            if (user == null || !user.isActive()) {
                log.warn("UserValidationClient: user {} is null or inactive", userId);
                throw new UserNotFoundException(userId);
            }

            return user;

        } catch (HttpClientErrorException.NotFound e) {
            log.warn("UserValidationClient: user {} not found in auth-service", userId);
            throw new UserNotFoundException(userId);

        } catch (ResourceAccessException e) {
            log.error("UserValidationClient: auth-service unreachable — {}", e.getMessage());
            throw new ServiceUnavailableException("Auth service is currently unavailable");
        }
    }
}

