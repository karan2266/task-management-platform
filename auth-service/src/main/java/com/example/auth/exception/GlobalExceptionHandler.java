package com.example.auth.exception;

import com.example.auth.dto.response.ErrorResponse;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authorization.AuthorizationDeniedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import com.fasterxml.jackson.databind.exc.InvalidFormatException;
import org.springframework.security.authentication.DisabledException;

import java.time.Instant;
import java.util.stream.Collectors;

@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

    @ExceptionHandler(EmailAlreadyExistsException.class)
    public ResponseEntity<ErrorResponse> handleEmailExists(
            EmailAlreadyExistsException ex, HttpServletRequest req) {
        return build(HttpStatus.CONFLICT, ex.getMessage(), req.getRequestURI());
    }

    @ExceptionHandler(UserNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleUserNotFound(
            UserNotFoundException ex, HttpServletRequest req) {
        return build(HttpStatus.NOT_FOUND, ex.getMessage(), req.getRequestURI());
    }

    @ExceptionHandler(BadCredentialsException.class)
    public ResponseEntity<ErrorResponse> handleBadCredentials(
            BadCredentialsException ex, HttpServletRequest req) {
        return build(HttpStatus.UNAUTHORIZED, "Invalid email or password", req.getRequestURI());
    }

    @ExceptionHandler(DisabledException.class)
    public ResponseEntity<ErrorResponse> handleDisabled(
            DisabledException ex, HttpServletRequest req) {
        return build(HttpStatus.UNAUTHORIZED, ex.getMessage(), req.getRequestURI());
    }

    @ExceptionHandler(TokenRefreshException.class)
    public ResponseEntity<ErrorResponse> handleTokenRefresh(
            TokenRefreshException ex, HttpServletRequest req) {
        return build(HttpStatus.UNAUTHORIZED, ex.getMessage(), req.getRequestURI());
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleValidation(
            MethodArgumentNotValidException ex, HttpServletRequest req) {
        String message = ex.getBindingResult().getFieldErrors().stream()
                .map(fe -> fe.getField() + ": " + fe.getDefaultMessage())
                .collect(Collectors.joining("; "));
        return build(HttpStatus.BAD_REQUEST, message, req.getRequestURI());
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ErrorResponse> handleMessageNotReadable(
            HttpMessageNotReadableException ex, HttpServletRequest req) {
        String message = "Malformed request body";
        Throwable cause = ex.getCause();
        if (cause instanceof InvalidFormatException ife && !ife.getPath().isEmpty()) {
            String field = ife.getPath().get(ife.getPath().size() - 1).getFieldName();
            String type  = ife.getTargetType().getSimpleName();
            message = "Invalid value for field '" + field + "': expected a valid " + type;
        }
        return build(HttpStatus.BAD_REQUEST, message, req.getRequestURI());
    }

    @ExceptionHandler({AccessDeniedException.class, AuthorizationDeniedException.class})
    public ResponseEntity<ErrorResponse> handleAccessDenied(
            RuntimeException ex, HttpServletRequest req) {
        return build(HttpStatus.FORBIDDEN, "Access denied: insufficient permissions", req.getRequestURI());
    }

    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<ErrorResponse> handleTypeMismatch(
            MethodArgumentTypeMismatchException ex, HttpServletRequest req) {
        String param    = ex.getName();
        String value    = String.valueOf(ex.getValue());
        String typeName = ex.getRequiredType() != null ? ex.getRequiredType().getSimpleName() : "unknown";
        String message  = "Invalid value '" + value + "' for parameter '" + param + "': expected a valid " + typeName;
        return build(HttpStatus.BAD_REQUEST, message, req.getRequestURI());
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleGeneral(
            Exception ex, HttpServletRequest req) {
        log.error("Unexpected error at [{}]: {}", req.getRequestURI(), ex.getMessage(), ex);
        return build(HttpStatus.INTERNAL_SERVER_ERROR, "An unexpected error occurred", req.getRequestURI());
    }

    // ── helpers ─────────────────────────────────────────────────────────────

    private ResponseEntity<ErrorResponse> build(HttpStatus status, String message, String path) {
        ErrorResponse body = ErrorResponse.builder()
                .timestamp(Instant.now())
                .status(status.value())
                .message(message)
                .path(path)
                .build();
        return ResponseEntity.status(status).body(body);
    }
}

