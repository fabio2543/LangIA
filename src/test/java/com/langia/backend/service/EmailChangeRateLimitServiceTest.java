package com.langia.backend.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.Duration;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.test.util.ReflectionTestUtils;

/**
 * Testes unitários para EmailChangeRateLimitService.
 */
@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class EmailChangeRateLimitServiceTest {

    @Mock
    private RedisTemplate<String, String> redisTemplate;

    @Mock
    private ValueOperations<String, String> valueOperations;

    private EmailChangeRateLimitService rateLimitService;

    private UUID userId;
    private String attemptKey;
    private String lockoutKey;

    @BeforeEach
    void setUp() {
        rateLimitService = new EmailChangeRateLimitService(redisTemplate);
        ReflectionTestUtils.setField(rateLimitService, "maxAttemptsPerWindow", 5);
        ReflectionTestUtils.setField(rateLimitService, "windowMinutes", 15);
        ReflectionTestUtils.setField(rateLimitService, "lockoutMinutes", 60);

        userId = UUID.randomUUID();
        attemptKey = "email_change:verify:" + userId;
        lockoutKey = "email_change:lockout:" + userId;

        when(redisTemplate.opsForValue()).thenReturn(valueOperations);
    }

    @Test
    void deveRetornarFalseQuandoNaoHaTentativas() {
        // Arrange
        when(redisTemplate.hasKey(lockoutKey)).thenReturn(false);
        when(valueOperations.get(attemptKey)).thenReturn(null);

        // Act
        boolean result = rateLimitService.isAttemptLimitReached(userId);

        // Assert
        assertFalse(result);
    }

    @Test
    void deveRetornarFalseQuandoTentativasAbaixoDoLimite() {
        // Arrange
        when(redisTemplate.hasKey(lockoutKey)).thenReturn(false);
        when(valueOperations.get(attemptKey)).thenReturn("3");

        // Act
        boolean result = rateLimitService.isAttemptLimitReached(userId);

        // Assert
        assertFalse(result);
    }

    @Test
    void deveRetornarTrueQuandoLimiteAtingido() {
        // Arrange
        when(redisTemplate.hasKey(lockoutKey)).thenReturn(false);
        when(valueOperations.get(attemptKey)).thenReturn("5");

        // Act
        boolean result = rateLimitService.isAttemptLimitReached(userId);

        // Assert
        assertTrue(result);
    }

    @Test
    void deveRetornarTrueQuandoUsuarioBloqueado() {
        // Arrange
        when(redisTemplate.hasKey(lockoutKey)).thenReturn(true);

        // Act
        boolean result = rateLimitService.isAttemptLimitReached(userId);

        // Assert
        assertTrue(result);
        verify(valueOperations, never()).get(anyString());
    }

    @Test
    void deveIncrementarTentativasEDefinirTTLNaPrimeiraTentativa() {
        // Arrange
        when(valueOperations.increment(attemptKey)).thenReturn(1L);

        // Act
        boolean locked = rateLimitService.recordFailedAttempt(userId);

        // Assert
        assertFalse(locked);
        verify(valueOperations).increment(attemptKey);
        verify(redisTemplate).expire(eq(attemptKey), any(Duration.class));
    }

    @Test
    void naoDeveDefinirTTLEmTentativasSubsequentes() {
        // Arrange
        when(valueOperations.increment(attemptKey)).thenReturn(2L);

        // Act
        boolean locked = rateLimitService.recordFailedAttempt(userId);

        // Assert
        assertFalse(locked);
        verify(valueOperations).increment(attemptKey);
        verify(redisTemplate, never()).expire(anyString(), any(Duration.class));
    }

    @Test
    void deveAplicarLockoutQuandoAtingeLimite() {
        // Arrange
        when(valueOperations.increment(attemptKey)).thenReturn(5L);

        // Act
        boolean locked = rateLimitService.recordFailedAttempt(userId);

        // Assert
        assertTrue(locked);
        verify(valueOperations).set(eq(lockoutKey), eq("locked"), any(Duration.class));
    }

    @Test
    void deveLimparTentativasCorretamente() {
        // Act
        rateLimitService.clearAttempts(userId);

        // Assert
        verify(redisTemplate).delete(attemptKey);
    }

    @Test
    void deveRetornarTentativasRestantesCorretamente() {
        // Arrange
        when(redisTemplate.hasKey(lockoutKey)).thenReturn(false);
        when(valueOperations.get(attemptKey)).thenReturn("2");

        // Act
        int remaining = rateLimitService.getRemainingAttempts(userId);

        // Assert
        assertEquals(3, remaining);
    }

    @Test
    void deveRetornarZeroTentativasQuandoBloqueado() {
        // Arrange
        when(redisTemplate.hasKey(lockoutKey)).thenReturn(true);

        // Act
        int remaining = rateLimitService.getRemainingAttempts(userId);

        // Assert
        assertEquals(0, remaining);
    }

    @Test
    void deveRetornarTempoRestanteDoLockout() {
        // Arrange
        when(redisTemplate.getExpire(lockoutKey)).thenReturn(3000L);

        // Act
        long timeRemaining = rateLimitService.getLockoutTimeRemaining(userId);

        // Assert
        assertEquals(3000L, timeRemaining);
    }

    @Test
    void deveResetarLockoutCorretamente() {
        // Act
        rateLimitService.resetLockout(userId);

        // Assert
        verify(redisTemplate).delete(attemptKey);
        verify(redisTemplate).delete(lockoutKey);
    }
}
