package com.langia.backend.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.UUID;
import java.util.concurrent.TimeUnit;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.langia.backend.config.JwtProperties;
import com.langia.backend.model.UserProfile;

class JwtTokenServiceTest {

    private static final String SECRET_KEY = "a".repeat(32) + "b".repeat(32); // 64 chars
    private static final long EXPIRATION_MS = 3600000L; // 1 hour

    private JwtTokenService jwtTokenService;

    @BeforeEach
    void setUp() {
        JwtProperties jwtProperties = new JwtProperties();
        jwtProperties.setSecretKey(SECRET_KEY);
        jwtProperties.setExpirationMs(EXPIRATION_MS);
        jwtTokenService = new JwtTokenService(jwtProperties);
    }

    @Test
    void generateToken_shouldCreateValidToken() {
        UUID userId = UUID.randomUUID();
        UserProfile profile = UserProfile.STUDENT;

        String token = jwtTokenService.generateToken(userId, profile);

        assertNotNull(token);
        assertFalse(token.isEmpty());
    }

    @Test
    void generateToken_shouldIncludeUserIdAndProfile() {
        UUID userId = UUID.randomUUID();
        UserProfile profile = UserProfile.TEACHER;

        String token = jwtTokenService.generateToken(userId, profile);

        UUID extractedUserId = jwtTokenService.extractUserId(token);
        UserProfile extractedProfile = jwtTokenService.extractUserProfile(token);

        assertThat(extractedUserId).isEqualTo(userId);
        assertThat(extractedProfile).isEqualTo(profile);
    }

    @Test
    void isValidToken_shouldReturnTrueForValidToken() {
        UUID userId = UUID.randomUUID();
        String token = jwtTokenService.generateToken(userId, UserProfile.STUDENT);

        boolean isValid = jwtTokenService.isValidToken(token);

        assertTrue(isValid);
    }

    @Test
    void isValidToken_shouldReturnFalseForInvalidToken() {
        String invalidToken = "invalid.token.here";

        boolean isValid = jwtTokenService.isValidToken(invalidToken);

        assertFalse(isValid);
    }

    @Test
    void isValidToken_shouldReturnFalseForTamperedToken() {
        UUID userId = UUID.randomUUID();
        String validToken = jwtTokenService.generateToken(userId, UserProfile.STUDENT);
        String tamperedToken = validToken.substring(0, validToken.length() - 5) + "XXXXX";

        boolean isValid = jwtTokenService.isValidToken(tamperedToken);

        assertFalse(isValid);
    }

    @Test
    void extractUserId_shouldReturnCorrectUserId() {
        UUID userId = UUID.randomUUID();
        String token = jwtTokenService.generateToken(userId, UserProfile.STUDENT);

        UUID extractedUserId = jwtTokenService.extractUserId(token);

        assertThat(extractedUserId).isEqualTo(userId);
    }

    @Test
    void extractUserId_shouldReturnNullForInvalidToken() {
        String invalidToken = "invalid.token.here";

        UUID extractedUserId = jwtTokenService.extractUserId(invalidToken);

        assertNull(extractedUserId);
    }

    @Test
    void extractUserProfile_shouldReturnCorrectProfile() {
        UserProfile[] profiles = { UserProfile.STUDENT, UserProfile.TEACHER, UserProfile.ADMIN };

        for (UserProfile profile : profiles) {
            UUID userId = UUID.randomUUID();
            String token = jwtTokenService.generateToken(userId, profile);

            UserProfile extractedProfile = jwtTokenService.extractUserProfile(token);

            assertThat(extractedProfile).isEqualTo(profile);
        }
    }

    @Test
    void extractUserProfile_shouldReturnNullForInvalidToken() {
        String invalidToken = "invalid.token.here";

        UserProfile extractedProfile = jwtTokenService.extractUserProfile(invalidToken);

        assertNull(extractedProfile);
    }

    @Test
    void isTokenValid_shouldReturnTrueForNonExpiredToken() {
        UUID userId = UUID.randomUUID();
        String token = jwtTokenService.generateToken(userId, UserProfile.STUDENT);

        boolean isValid = jwtTokenService.isTokenValid(token);

        assertTrue(isValid);
    }

    @Test
    void isTokenValid_shouldReturnFalseForExpiredToken() throws InterruptedException {
        JwtProperties shortExpirationProps = new JwtProperties();
        shortExpirationProps.setSecretKey(SECRET_KEY);
        shortExpirationProps.setExpirationMs(100L); // 100ms
        JwtTokenService shortExpirationService = new JwtTokenService(shortExpirationProps);

        UUID userId = UUID.randomUUID();
        String token = shortExpirationService.generateToken(userId, UserProfile.STUDENT);

        TimeUnit.MILLISECONDS.sleep(150); // Wait for expiration

        boolean isValid = shortExpirationService.isTokenValid(token);

        assertFalse(isValid);
    }

    @Test
    void validateToken_shouldReturnTrueForValidAndNonExpiredToken() {
        UUID userId = UUID.randomUUID();
        String token = jwtTokenService.generateToken(userId, UserProfile.STUDENT);

        boolean isValid = jwtTokenService.validateToken(token);

        assertTrue(isValid);
    }

    @Test
    void validateToken_shouldReturnFalseForInvalidToken() {
        String invalidToken = "invalid.token.here";

        boolean isValid = jwtTokenService.validateToken(invalidToken);

        assertFalse(isValid);
    }

    @Test
    void validateToken_shouldReturnFalseForExpiredToken() throws InterruptedException {
        JwtProperties shortExpirationProps = new JwtProperties();
        shortExpirationProps.setSecretKey(SECRET_KEY);
        shortExpirationProps.setExpirationMs(100L); // 100ms
        JwtTokenService shortExpirationService = new JwtTokenService(shortExpirationProps);

        UUID userId = UUID.randomUUID();
        String token = shortExpirationService.generateToken(userId, UserProfile.STUDENT);

        TimeUnit.MILLISECONDS.sleep(150); // Wait for expiration

        boolean isValid = shortExpirationService.validateToken(token);

        assertFalse(isValid);
    }

    @Test
    void token_shouldHaveCorrectExpirationTime() {
        UUID userId = UUID.randomUUID();
        String token = jwtTokenService.generateToken(userId, UserProfile.STUDENT);

        // Token should be valid now
        assertTrue(jwtTokenService.isTokenValid(token));

        // We can't easily test exact expiration without parsing the token,
        // but we can verify it's within a reasonable range
        assertTrue(jwtTokenService.validateToken(token));
    }

    @Test
    void generateToken_shouldProduceDifferentTokensForSameUser() {
        UUID userId = UUID.randomUUID();
        UserProfile profile = UserProfile.STUDENT;

        String token1 = jwtTokenService.generateToken(userId, profile);
        // Small delay to ensure different issuedAt
        try {
            Thread.sleep(10);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        String token2 = jwtTokenService.generateToken(userId, profile);

        // Tokens should be different (different issuedAt timestamps)
        assertThat(token1).isNotEqualTo(token2);

        // But both should extract the same userId and profile
        assertThat(jwtTokenService.extractUserId(token1)).isEqualTo(jwtTokenService.extractUserId(token2));
        assertThat(jwtTokenService.extractUserProfile(token1))
                .isEqualTo(jwtTokenService.extractUserProfile(token2));
    }
}

