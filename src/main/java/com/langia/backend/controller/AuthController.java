package com.langia.backend.controller;

import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.langia.backend.dto.LoginRequestDTO;
import com.langia.backend.dto.LoginResponseDTO;
import com.langia.backend.dto.UserSessionDTO;
import com.langia.backend.exception.InvalidCredentialsException;
import com.langia.backend.service.AuthenticationService;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * REST controller for authentication endpoints.
 * Handles login, logout, and session validation.
 */
@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
@Slf4j
public class AuthController {

    private static final String BEARER_PREFIX = "Bearer ";

    private final AuthenticationService authenticationService;

    /**
     * Endpoint to authenticate a user with email and password.
     * Validates the request DTO, calls the authentication service, and returns
     * a login response with JWT token, user data, and permissions.
     *
     * @param loginRequest DTO containing email and password
     * @return ResponseEntity with LoginResponseDTO and status 200 on success,
     *         or status 401 if credentials are invalid
     */
    @PostMapping("/login")
    public ResponseEntity<?> login(@Valid @RequestBody LoginRequestDTO loginRequest) {
        log.debug("Login request received for email: {}", loginRequest.getEmail());

        try {
            LoginResponseDTO response = authenticationService.login(
                    loginRequest.getEmail(),
                    loginRequest.getPassword());

            log.info("Login successful for email: {}", loginRequest.getEmail());
            return ResponseEntity.ok(response);
        } catch (InvalidCredentialsException ex) {
            log.warn("Login failed for email: {} - invalid credentials", loginRequest.getEmail());
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(createErrorResponse("Authentication error", ex.getMessage()));
        } catch (Exception ex) {
            log.error("Unexpected error during login for email: {}", loginRequest.getEmail(), ex);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(createErrorResponse("Internal server error", "An unexpected error occurred"));
        }
    }

    /**
     * Endpoint to logout a user by invalidating their session.
     * Extracts the JWT token from the Authorization header and removes
     * the session from Redis.
     *
     * @param request HTTP request containing Authorization header
     * @return ResponseEntity with status 200 (or 204) on success,
     *         or status 401 if token is missing or invalid
     */
    @PostMapping("/logout")
    public ResponseEntity<?> logout(HttpServletRequest request) {
        log.debug("Logout request received");

        String token = extractToken(request);
        if (!StringUtils.hasText(token)) {
            log.warn("Logout failed: missing or invalid Authorization header");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(createErrorResponse("Authentication error", "Missing or invalid token"));
        }

        try {
            authenticationService.logout(token);
            log.info("Logout successful");
            return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
        } catch (Exception ex) {
            log.error("Unexpected error during logout", ex);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(createErrorResponse("Internal server error", "An unexpected error occurred"));
        }
    }

    /**
     * Endpoint to validate if a session is still valid.
     * Useful for debugging and for frontend to check session state.
     *
     * @param request HTTP request containing Authorization header
     * @return ResponseEntity with UserSessionDTO and status 200 if valid,
     *         or status 401 if token is missing or invalid
     */
    @GetMapping("/validate")
    public ResponseEntity<?> validateSession(HttpServletRequest request) {
        log.debug("Session validation request received");

        String token = extractToken(request);
        if (!StringUtils.hasText(token)) {
            log.warn("Session validation failed: missing or invalid Authorization header");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(createErrorResponse("Authentication error", "Missing or invalid token"));
        }

        try {
            UserSessionDTO session = authenticationService.validateSession(token);
            if (session == null) {
                log.debug("Session validation failed: invalid or expired session");
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body(createErrorResponse("Authentication error", "Invalid or expired session"));
            }

            log.debug("Session validation successful for user ID: {}", session.getUserId());
            return ResponseEntity.ok(session);
        } catch (Exception ex) {
            log.error("Unexpected error during session validation", ex);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(createErrorResponse("Internal server error", "An unexpected error occurred"));
        }
    }

    /**
     * Extracts the JWT token from the Authorization header.
     * Expects format: "Bearer <token>"
     *
     * @param request HTTP request
     * @return extracted token, or null if not found or invalid format
     */
    private String extractToken(HttpServletRequest request) {
        String authHeader = request.getHeader(HttpHeaders.AUTHORIZATION);
        if (!StringUtils.hasText(authHeader) || !authHeader.startsWith(BEARER_PREFIX)) {
            return null;
        }
        return authHeader.substring(BEARER_PREFIX.length());
    }

    /**
     * Creates a simple error response map.
     *
     * @param error   error type
     * @param message error message
     * @return map with error and message keys
     */
    private java.util.Map<String, String> createErrorResponse(String error, String message) {
        java.util.Map<String, String> response = new java.util.HashMap<>();
        response.put("error", error);
        response.put("message", message);
        return response;
    }
}

