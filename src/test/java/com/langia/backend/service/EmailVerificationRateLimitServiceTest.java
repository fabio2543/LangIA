package com.langia.backend.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.Duration;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.test.util.ReflectionTestUtils;

/**
 * Testes para o serviço de rate limiting de verificação de email.
 */
@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class EmailVerificationRateLimitServiceTest {

    @Mock
    private RedisTemplate<String, String> redisTemplate;

    @Mock
    private ValueOperations<String, String> valueOperations;

    @InjectMocks
    private EmailVerificationRateLimitService rateLimitService;

    private UUID testUserId;

    @BeforeEach
    void setUp() {
        testUserId = UUID.randomUUID();
        ReflectionTestUtils.setField(rateLimitService, "maxResendsPerHour", 3);
        ReflectionTestUtils.setField(rateLimitService, "windowHours", 1);
        when(redisTemplate.opsForValue()).thenReturn(valueOperations);
    }

    // ========== Testes de Limite de Reenvios ==========

    @Test
    void deveRetornarFalseQuandoNaoHaTentativas() {
        // Arrange
        when(valueOperations.get(anyString())).thenReturn(null);

        // Act
        boolean result = rateLimitService.isResendLimitReached(testUserId);

        // Assert
        assertFalse(result);
    }

    @Test
    void deveRetornarFalseQuandoAbaixoDoLimite() {
        // Arrange
        when(valueOperations.get(anyString())).thenReturn("2");

        // Act
        boolean result = rateLimitService.isResendLimitReached(testUserId);

        // Assert
        assertFalse(result);
    }

    @Test
    void deveRetornarTrueQuandoAtingeLimite() {
        // Arrange
        when(valueOperations.get(anyString())).thenReturn("3");

        // Act
        boolean result = rateLimitService.isResendLimitReached(testUserId);

        // Assert
        assertTrue(result);
    }

    @Test
    void deveRetornarTrueQuandoAcimaDoLimite() {
        // Arrange
        when(valueOperations.get(anyString())).thenReturn("5");

        // Act
        boolean result = rateLimitService.isResendLimitReached(testUserId);

        // Assert
        assertTrue(result);
    }

    // ========== Testes de Registro de Tentativas ==========

    @Test
    void deveRegistrarTentativaEDefinirTTL() {
        // Arrange
        when(valueOperations.increment(anyString())).thenReturn(1L);

        // Act
        rateLimitService.recordResendAttempt(testUserId);

        // Assert
        verify(valueOperations).increment(anyString());
        verify(redisTemplate).expire(anyString(), any(Duration.class));
    }

    @Test
    void naoDeveRedefinirTTLEmTentativasSubsequentes() {
        // Arrange
        when(valueOperations.increment(anyString())).thenReturn(2L);

        // Act
        rateLimitService.recordResendAttempt(testUserId);

        // Assert
        verify(valueOperations).increment(anyString());
    }

    // ========== Testes de Reenvios Restantes ==========

    @Test
    void deveRetornarReenviosRestantesCorretamente() {
        // Arrange
        when(valueOperations.get(anyString())).thenReturn("1");

        // Act
        int remaining = rateLimitService.getRemainingResends(testUserId);

        // Assert
        assertEquals(2, remaining);
    }

    @Test
    void deveRetornarMaximoQuandoNaoHaTentativas() {
        // Arrange
        when(valueOperations.get(anyString())).thenReturn(null);

        // Act
        int remaining = rateLimitService.getRemainingResends(testUserId);

        // Assert
        assertEquals(3, remaining);
    }

    @Test
    void deveRetornarZeroQuandoLimiteAtingido() {
        // Arrange
        when(valueOperations.get(anyString())).thenReturn("3");

        // Act
        int remaining = rateLimitService.getRemainingResends(testUserId);

        // Assert
        assertEquals(0, remaining);
    }

    // ========== Testes de Tempo até Reset ==========

    @Test
    void deveRetornarTTLCorreto() {
        // Arrange
        when(redisTemplate.getExpire(anyString())).thenReturn(1800L);

        // Act
        long ttl = rateLimitService.getTimeUntilReset(testUserId);

        // Assert
        assertEquals(1800L, ttl);
    }

    @Test
    void deveRetornarMenosUmQuandoNaoHaTTL() {
        // Arrange
        when(redisTemplate.getExpire(anyString())).thenReturn(null);

        // Act
        long ttl = rateLimitService.getTimeUntilReset(testUserId);

        // Assert
        assertEquals(-1L, ttl);
    }

    // ========== Testes de Reset ==========

    @Test
    void deveResetarTentativasDoUsuario() {
        // Act
        rateLimitService.resetResendAttempts(testUserId);

        // Assert
        verify(redisTemplate).delete(anyString());
    }
}
