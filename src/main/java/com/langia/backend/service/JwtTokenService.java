package com.langia.backend.service;

import java.util.Date;
import java.util.UUID;

import javax.crypto.SecretKey;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.langia.backend.config.JwtProperties;
import com.langia.backend.model.UserProfile;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import lombok.extern.slf4j.Slf4j;

/**
 * Service responsible for managing JWT tokens.
 * Handles token generation, validation, and information extraction.
 */
@Service
@Slf4j
public class JwtTokenService {

    private final JwtProperties jwtProperties;
    private final SecretKey secretKey;

    @Autowired
    public JwtTokenService(JwtProperties jwtProperties) {
        this.jwtProperties = jwtProperties;
        this.secretKey = Keys.hmacShaKeyFor(jwtProperties.getSecretKey().getBytes());
        log.info("JwtTokenService initialized with expiration: {} ms", jwtProperties.getExpirationMs());
    }

    /**
     * Generates a new JWT token for a user after successful login.
     *
     * @param userId   The user's unique identifier
     * @param profile  The user's profile (STUDENT, TEACHER, ADMIN)
     * @return A signed JWT token string
     */
    public String generateToken(UUID userId, UserProfile profile) {
        log.debug("Generating JWT token for user ID: {} with profile: {}", userId, profile);

        Date now = new Date();
        Date expiration = new Date(now.getTime() + jwtProperties.getExpirationMs());

        String token = Jwts.builder()
                .subject(userId.toString())
                .claim("profile", profile.name())
                .issuedAt(now)
                .expiration(expiration)
                .signWith(secretKey)
                .compact();

        log.info("JWT token generated successfully for user ID: {}", userId);
        return token;
    }

    /**
     * Validates if a token is authentic and has not been tampered with.
     *
     * @param token The JWT token string to validate
     * @return true if the token is valid and authentic, false otherwise
     */
    public boolean isValidToken(String token) {
        try {
            log.debug("Validating JWT token");
            Jwts.parser()
                    .verifyWith(secretKey)
                    .build()
                    .parseSignedClaims(token);
            log.debug("JWT token is valid");
            return true;
        } catch (Exception e) {
            log.warn("JWT token validation failed: {}", e.getMessage());
            return false;
        }
    }

    /**
     * Extracts the user ID from a JWT token.
     *
     * @param token The JWT token string
     * @return The user's UUID, or null if token is invalid
     */
    public UUID extractUserId(String token) {
        try {
            log.debug("Extracting user ID from JWT token");
            Claims claims = Jwts.parser()
                    .verifyWith(secretKey)
                    .build()
                    .parseSignedClaims(token)
                    .getPayload();

            String userIdString = claims.getSubject();
            UUID userId = UUID.fromString(userIdString);
            log.debug("User ID extracted successfully: {}", userId);
            return userId;
        } catch (Exception e) {
            log.error("Failed to extract user ID from token: {}", e.getMessage());
            return null;
        }
    }

    /**
     * Extracts the user profile from a JWT token.
     *
     * @param token The JWT token string
     * @return The user's profile, or null if token is invalid
     */
    public UserProfile extractUserProfile(String token) {
        try {
            log.debug("Extracting user profile from JWT token");
            Claims claims = Jwts.parser()
                    .verifyWith(secretKey)
                    .build()
                    .parseSignedClaims(token)
                    .getPayload();

            String profileString = claims.get("profile", String.class);
            UserProfile profile = UserProfile.valueOf(profileString);
            log.debug("User profile extracted successfully: {}", profile);
            return profile;
        } catch (Exception e) {
            log.error("Failed to extract user profile from token: {}", e.getMessage());
            return null;
        }
    }

    /**
     * Checks if a token is still within its validity period (1 hour).
     *
     * @param token The JWT token string
     * @return true if the token is not expired, false otherwise
     */
    public boolean isTokenValid(String token) {
        try {
            log.debug("Checking token expiration");
            Claims claims = Jwts.parser()
                    .verifyWith(secretKey)
                    .build()
                    .parseSignedClaims(token)
                    .getPayload();

            Date expiration = claims.getExpiration();
            boolean isValid = expiration.after(new Date());

            if (isValid) {
                log.debug("Token is still valid, expires at: {}", expiration);
            } else {
                log.warn("Token has expired at: {}", expiration);
            }

            return isValid;
        } catch (Exception e) {
            log.warn("Failed to check token expiration: {}", e.getMessage());
            return false;
        }
    }

    /**
     * Validates token authenticity and expiration in a single call.
     * This is a convenience method that combines isValidToken and isTokenValid.
     *
     * @param token The JWT token string
     * @return true if token is authentic and not expired, false otherwise
     */
    public boolean validateToken(String token) {
        if (!isValidToken(token)) {
            return false;
        }
        return isTokenValid(token);
    }
}

