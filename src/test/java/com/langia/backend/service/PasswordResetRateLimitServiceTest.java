package com.langia.backend.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.Duration;

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

import com.langia.backend.config.PasswordResetProperties;

/**
 * Testes para o serviço de rate limiting de recuperação de senha.
 */
@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class PasswordResetRateLimitServiceTest {

    @Mock
    private RedisTemplate<String, String> redisTemplate;

    @Mock
    private ValueOperations<String, String> valueOperations;

    @Mock
    private PasswordResetProperties properties;

    @Mock
    private PasswordResetProperties.RateLimitConfig rateLimitConfig;

    @InjectMocks
    private PasswordResetRateLimitService rateLimitService;

    private String testIpAddress;
    private String testEmail;

    @BeforeEach
    void setUp() {
        testIpAddress = "192.168.1.1";
        testEmail = "test@example.com";
        when(redisTemplate.opsForValue()).thenReturn(valueOperations);
    }

    // ========== Testes de Bloqueio de IP ==========

    @Test
    void deveRetornarFalseQuandoIpNaoTemTentativas() {
        // Arrange
        when(valueOperations.get(anyString())).thenReturn(null);

        // Act
        boolean result = rateLimitService.isIpBlocked(testIpAddress);

        // Assert
        assertFalse(result);
    }

    @Test
    void deveRetornarFalseQuandoIpAbaixoDoLimite() {
        // Arrange
        when(valueOperations.get(anyString())).thenReturn("5");
        when(properties.getRateLimit()).thenReturn(rateLimitConfig);
        when(rateLimitConfig.getMaxAttemptsPerIp()).thenReturn(10);

        // Act
        boolean result = rateLimitService.isIpBlocked(testIpAddress);

        // Assert
        assertFalse(result);
    }

    @Test
    void deveRetornarTrueQuandoIpAtingeLimite() {
        // Arrange
        when(valueOperations.get(anyString())).thenReturn("10");
        when(properties.getRateLimit()).thenReturn(rateLimitConfig);
        when(rateLimitConfig.getMaxAttemptsPerIp()).thenReturn(10);

        // Act
        boolean result = rateLimitService.isIpBlocked(testIpAddress);

        // Assert
        assertTrue(result);
    }

    // ========== Testes de Registro de Tentativas ==========

    @Test
    void deveRegistrarTentativaEDefinirTTL() {
        // Arrange
        when(valueOperations.increment(anyString())).thenReturn(1L);
        when(properties.getRateLimit()).thenReturn(rateLimitConfig);
        when(rateLimitConfig.getWindowHours()).thenReturn(1);

        // Act
        rateLimitService.recordAttempt(testIpAddress);

        // Assert
        verify(valueOperations).increment(anyString());
        verify(redisTemplate).expire(anyString(), any(Duration.class));
    }

    @Test
    void naoDeveRedefinirTTLEmTentativasSubsequentes() {
        // Arrange
        when(valueOperations.increment(anyString())).thenReturn(2L);

        // Act
        rateLimitService.recordAttempt(testIpAddress);

        // Assert
        verify(valueOperations).increment(anyString());
    }

    // ========== Testes de Tentativas Restantes ==========

    @Test
    void deveRetornarTentativasRestantesCorretamente() {
        // Arrange
        when(valueOperations.get(anyString())).thenReturn("3");
        when(properties.getRateLimit()).thenReturn(rateLimitConfig);
        when(rateLimitConfig.getMaxAttemptsPerIp()).thenReturn(10);

        // Act
        int remaining = rateLimitService.getRemainingAttempts(testIpAddress);

        // Assert
        assertEquals(7, remaining);
    }

    @Test
    void deveRetornarMaximoQuandoNaoHaTentativas() {
        // Arrange
        when(valueOperations.get(anyString())).thenReturn(null);
        when(properties.getRateLimit()).thenReturn(rateLimitConfig);
        when(rateLimitConfig.getMaxAttemptsPerIp()).thenReturn(10);

        // Act
        int remaining = rateLimitService.getRemainingAttempts(testIpAddress);

        // Assert
        assertEquals(10, remaining);
    }

    // ========== Testes de Tempo até Reset ==========

    @Test
    void deveRetornarTTLCorreto() {
        // Arrange
        when(redisTemplate.getExpire(anyString())).thenReturn(3600L);

        // Act
        long ttl = rateLimitService.getTimeUntilReset(testIpAddress);

        // Assert
        assertEquals(3600L, ttl);
    }

    @Test
    void deveRetornarMenosUmQuandoNaoHaTTL() {
        // Arrange
        when(redisTemplate.getExpire(anyString())).thenReturn(null);

        // Act
        long ttl = rateLimitService.getTimeUntilReset(testIpAddress);

        // Assert
        assertEquals(-1L, ttl);
    }

    // ========== Testes de Reset ==========

    @Test
    void deveResetarTentativasDoIp() {
        // Act
        rateLimitService.resetAttempts(testIpAddress);

        // Assert
        verify(redisTemplate).delete(anyString());
    }

    // ========== Testes de Rate Limiting por Email ==========

    @Test
    void deveRetornarFalseQuandoEmailAbaixoDoLimite() {
        // Arrange
        when(valueOperations.get(anyString())).thenReturn("2");
        when(properties.getRateLimit()).thenReturn(rateLimitConfig);
        when(rateLimitConfig.getMaxAttemptsPerEmail()).thenReturn(5);

        // Act
        boolean result = rateLimitService.isEmailLimitReached(testEmail);

        // Assert
        assertFalse(result);
    }

    @Test
    void deveRetornarTrueQuandoEmailAtingeLimite() {
        // Arrange
        when(valueOperations.get(anyString())).thenReturn("5");
        when(properties.getRateLimit()).thenReturn(rateLimitConfig);
        when(rateLimitConfig.getMaxAttemptsPerEmail()).thenReturn(5);

        // Act
        boolean result = rateLimitService.isEmailLimitReached(testEmail);

        // Assert
        assertTrue(result);
    }

    @Test
    void deveRegistrarTentativaPorEmailEDefinirTTL() {
        // Arrange
        when(valueOperations.increment(anyString())).thenReturn(1L);
        when(properties.getRateLimit()).thenReturn(rateLimitConfig);
        when(rateLimitConfig.getWindowHours()).thenReturn(1);

        // Act
        rateLimitService.recordEmailAttempt(testEmail);

        // Assert
        verify(valueOperations).increment(anyString());
        verify(redisTemplate).expire(anyString(), any(Duration.class));
    }

    @Test
    void deveResetarTentativasDoEmail() {
        // Act
        rateLimitService.resetEmailAttempts(testEmail);

        // Assert
        verify(redisTemplate).delete(anyString());
    }
}
