package com.langia.backend.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.Duration;
import java.util.Set;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;

import com.langia.backend.config.JwtProperties;
import com.langia.backend.dto.UserSessionDTO;
import com.langia.backend.model.UserProfile;

@ExtendWith(MockitoExtension.class)
@SuppressWarnings("null")
class UserSessionServiceTest {

    private static final String SECRET_KEY = "a".repeat(32) + "b".repeat(32);
    private static final long EXPIRATION_MS = 3600000L;

    @Mock
    private RedisTemplate<String, UserSessionDTO> redisTemplate;

    @Mock
    private ValueOperations<String, UserSessionDTO> valueOperations;

    private UserSessionService userSessionService;

    @BeforeEach
    void setUp() {
        JwtProperties jwtProperties = new JwtProperties();
        jwtProperties.setSecretKey(SECRET_KEY);
        jwtProperties.setExpirationMs(EXPIRATION_MS);

        when(redisTemplate.opsForValue()).thenReturn(valueOperations);

        userSessionService = new UserSessionService(redisTemplate, jwtProperties);
    }

    @Test
    void saveSession_shouldStoreSessionWithCorrectKey() {
        String token = "test.jwt.token";
        UserSessionDTO sessionData = createTestSession();

        userSessionService.saveSession(token, sessionData);

        ArgumentCaptor<String> keyCaptor = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<UserSessionDTO> valueCaptor = ArgumentCaptor.forClass(UserSessionDTO.class);
        ArgumentCaptor<Duration> ttlCaptor = ArgumentCaptor.forClass(Duration.class);

        verify(valueOperations).set(keyCaptor.capture(), valueCaptor.capture(), ttlCaptor.capture());

        String capturedKey = keyCaptor.getValue();
        assertThat(capturedKey).startsWith("session:");
        assertThat(capturedKey).hasSizeGreaterThan("session:".length());

        UserSessionDTO capturedValue = valueCaptor.getValue();
        assertThat(capturedValue).isEqualTo(sessionData);

        Duration capturedTtl = ttlCaptor.getValue();
        assertThat(capturedTtl).isEqualTo(Duration.ofMillis(EXPIRATION_MS));
    }

    @Test
    void saveSession_shouldThrowExceptionWhenSessionDataIsNull() {
        String token = "test.jwt.token";

        assertThatThrownBy(() -> userSessionService.saveSession(token, null))
                .isInstanceOf(NullPointerException.class)
                .hasMessageContaining("Session data must not be null");

        verify(valueOperations, never()).set(anyString(), any(), any(Duration.class));
    }

    @Test
    void saveSession_shouldThrowExceptionWhenTokenIsNull() {
        UserSessionDTO sessionData = createTestSession();

        assertThatThrownBy(() -> userSessionService.saveSession(null, sessionData))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Token must not be null or empty");
    }

    @Test
    void saveSession_shouldThrowExceptionWhenTokenIsEmpty() {
        UserSessionDTO sessionData = createTestSession();

        assertThatThrownBy(() -> userSessionService.saveSession("", sessionData))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Token must not be null or empty");
    }

    @Test
    void getSession_shouldReturnStoredSession() {
        String token = "test.jwt.token";
        UserSessionDTO expectedSession = createTestSession();

        when(valueOperations.get(anyString())).thenReturn(expectedSession);

        UserSessionDTO retrievedSession = userSessionService.getSession(token);

        assertThat(retrievedSession).isEqualTo(expectedSession);
        verify(valueOperations).get(anyString());
    }

    @Test
    void getSession_shouldReturnNullWhenSessionNotFound() {
        String token = "test.jwt.token";

        when(valueOperations.get(anyString())).thenReturn(null);

        UserSessionDTO retrievedSession = userSessionService.getSession(token);

        assertThat(retrievedSession).isNull();
        verify(valueOperations).get(anyString());
    }

    @Test
    void getSession_shouldThrowExceptionWhenTokenIsNull() {
        assertThatThrownBy(() -> userSessionService.getSession(null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Token must not be null or empty");
    }

    @Test
    void removeSession_shouldDeleteSessionFromRedis() {
        String token = "test.jwt.token";

        when(redisTemplate.delete(anyString())).thenReturn(true);

        userSessionService.removeSession(token);

        verify(redisTemplate).delete(anyString());
    }

    @Test
    void removeSession_shouldHandleNonExistentSession() {
        String token = "test.jwt.token";

        when(redisTemplate.delete(anyString())).thenReturn(false);

        userSessionService.removeSession(token);

        verify(redisTemplate).delete(anyString());
    }

    @Test
    void removeSession_shouldThrowExceptionWhenTokenIsNull() {
        assertThatThrownBy(() -> userSessionService.removeSession(null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Token must not be null or empty");
    }

    @Test
    void saveSession_shouldHashTokenInKey() {
        String token1 = "token1";
        String token2 = "token2";
        UserSessionDTO sessionData = createTestSession();

        userSessionService.saveSession(token1, sessionData);
        userSessionService.saveSession(token2, sessionData);

        ArgumentCaptor<String> keyCaptor = ArgumentCaptor.forClass(String.class);
        verify(valueOperations, times(2)).set(keyCaptor.capture(), any(UserSessionDTO.class), any(Duration.class));

        String key1 = keyCaptor.getAllValues().get(0);
        String key2 = keyCaptor.getAllValues().get(1);

        // Keys should be different and both should start with "session:"
        assertThat(key1).startsWith("session:");
        assertThat(key2).startsWith("session:");
        assertThat(key1).isNotEqualTo(key2);
    }

    @Test
    void getSession_shouldUseHashedKey() {
        String token = "test.jwt.token";
        UserSessionDTO expectedSession = createTestSession();

        when(valueOperations.get(anyString())).thenReturn(expectedSession);

        userSessionService.getSession(token);

        ArgumentCaptor<String> keyCaptor = ArgumentCaptor.forClass(String.class);
        verify(valueOperations).get(keyCaptor.capture());

        String capturedKey = keyCaptor.getValue();
        assertThat(capturedKey).startsWith("session:");
        assertThat(capturedKey).hasSizeGreaterThan("session:".length());
    }

    @Test
    void removeSession_shouldUseHashedKey() {
        String token = "test.jwt.token";

        when(redisTemplate.delete(anyString())).thenReturn(true);

        userSessionService.removeSession(token);

        ArgumentCaptor<String> keyCaptor = ArgumentCaptor.forClass(String.class);
        verify(redisTemplate).delete(keyCaptor.capture());

        String capturedKey = keyCaptor.getValue();
        assertThat(capturedKey).startsWith("session:");
        assertThat(capturedKey).hasSizeGreaterThan("session:".length());
    }

    private UserSessionDTO createTestSession() {
        return UserSessionDTO.builder()
                .userId(UUID.randomUUID())
                .name("Test User")
                .email("test@example.com")
                .profile(UserProfile.STUDENT)
                .permissions(Set.of("permission1", "permission2"))
                .expiresAt(System.currentTimeMillis() + EXPIRATION_MS)
                .build();
    }
}

