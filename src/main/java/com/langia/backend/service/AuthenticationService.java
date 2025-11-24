package com.langia.backend.service;

import java.util.Objects;
import java.util.Set;
import java.util.UUID;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import com.langia.backend.config.JwtProperties;
import com.langia.backend.dto.LoginResponseDTO;
import com.langia.backend.dto.UserSessionDTO;
import com.langia.backend.exception.InvalidCredentialsException;
import com.langia.backend.model.User;
import com.langia.backend.repository.UserRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * Central authentication service that orchestrates the entire login process.
 * Coordinates database, JWT, Redis, and permissions components to perform
 * complete authentication flow.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class AuthenticationService {

    private static final String INVALID_CREDENTIALS_MESSAGE = "Invalid email or password";

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenService jwtTokenService;
    private final UserSessionService userSessionService;
    private final PermissionService permissionService;
    private final JwtProperties jwtProperties;

    /**
     * Authenticates a user with email and password.
     * Validates credentials, generates JWT token, saves session in Redis,
     * and returns complete login response with user data and permissions.
     *
     * @param email    user's email (will be normalized)
     * @param password user's plain text password
     * @return LoginResponseDTO with token, user data, and permissions
     * @throws InvalidCredentialsException if email doesn't exist or password is incorrect
     */
    @Transactional(readOnly = true)
    public LoginResponseDTO login(String email, String password) {
        log.debug("Login attempt for email: {}", email);

        // Normalize email
        String normalizedEmail = normalizeEmail(email);

        // Find user by email
        User user = userRepository.findByEmail(normalizedEmail)
                .orElseThrow(() -> {
                    log.warn("Authentication failed for email: {} - user not found", normalizedEmail);
                    return new InvalidCredentialsException(INVALID_CREDENTIALS_MESSAGE);
                });

        // Verify password
        if (!passwordEncoder.matches(password, user.getPassword())) {
            log.warn("Authentication failed for email: {} - invalid password", normalizedEmail);
            throw new InvalidCredentialsException(INVALID_CREDENTIALS_MESSAGE);
        }

        // Generate JWT token
        String token = jwtTokenService.generateToken(user.getId(), user.getProfile());
        log.debug("JWT token generated for user ID: {}", user.getId());

        // Get user permissions
        Set<String> permissions = permissionService.getPermissions(user.getProfile());
        log.debug("Retrieved {} permissions for user profile: {}", permissions.size(), user.getProfile());

        // Calculate expiration time
        long expiresAt = System.currentTimeMillis() + jwtProperties.getExpirationMs();

        // Create session DTO
        UserSessionDTO sessionDTO = UserSessionDTO.builder()
                .userId(user.getId())
                .name(user.getName())
                .email(user.getEmail())
                .profile(user.getProfile())
                .permissions(permissions)
                .expiresAt(expiresAt)
                .build();

        // Save session in Redis
        userSessionService.saveSession(token, sessionDTO);
        log.debug("Session saved in Redis for user ID: {}", user.getId());

        // Build and return response
        LoginResponseDTO response = LoginResponseDTO.fromUser(
                user,
                token,
                permissions,
                jwtProperties.getExpirationMs());

        log.info("User {} successfully authenticated", user.getId());
        return response;
    }

    /**
     * Validates if a session is still valid by checking the token and Redis.
     *
     * @param token JWT token to validate
     * @return UserSessionDTO if session is valid, null otherwise
     */
    public UserSessionDTO validateSession(String token) {
        if (!StringUtils.hasText(token)) {
            log.debug("Session validation failed: empty token");
            return null;
        }

        log.debug("Validating session for token");

        // Validate token structure and expiration
        if (!jwtTokenService.validateToken(token)) {
            log.debug("Session validation failed: invalid or expired token");
            return null;
        }

        // Check if session exists in Redis
        UserSessionDTO session = userSessionService.getSession(token);
        if (session == null) {
            log.debug("Session validation failed: session not found in Redis");
            return null;
        }

        log.debug("Session validated successfully for user ID: {}", session.getUserId());
        return session;
    }

    /**
     * Logs out a user by removing their session from Redis.
     * This invalidates the token immediately, even if it hasn't expired naturally.
     *
     * @param token JWT token to invalidate
     */
    public void logout(String token) {
        if (!StringUtils.hasText(token)) {
            log.debug("Logout attempted with empty token");
            return;
        }

        log.debug("Logout requested for token");

        // Extract user ID from token for logging
        UUID userId = jwtTokenService.extractUserId(token);

        // Remove session from Redis
        userSessionService.removeSession(token);

        if (userId != null) {
            log.info("User {} logged out", userId);
        } else {
            log.info("Session removed for token (user ID could not be extracted)");
        }
    }

    /**
     * Normalizes email by trimming whitespace and converting to lowercase.
     *
     * @param email email to normalize
     * @return normalized email
     * @throws IllegalArgumentException if email is null or empty
     */
    private String normalizeEmail(String email) {
        Objects.requireNonNull(email, "Email must not be null");
        String normalized = email.trim().toLowerCase();
        if (!StringUtils.hasText(normalized)) {
            throw new IllegalArgumentException("Email must not be empty");
        }
        return normalized;
    }
}

