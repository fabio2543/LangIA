package com.langia.backend.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.test.util.ReflectionTestUtils;

import com.langia.backend.config.PasswordResetProperties;
import com.langia.backend.dto.ValidateTokenResponseDTO;
import com.langia.backend.exception.InvalidResetTokenException;
import com.langia.backend.exception.PasswordRecentlyUsedException;
import com.langia.backend.exception.PasswordValidationException;
import com.langia.backend.exception.RateLimitExceededException;
import com.langia.backend.model.PasswordHistory;
import com.langia.backend.model.PasswordResetToken;
import com.langia.backend.model.Profile;
import com.langia.backend.model.User;
import com.langia.backend.model.UserProfile;
import com.langia.backend.repository.PasswordHistoryRepository;
import com.langia.backend.repository.PasswordResetTokenRepository;
import com.langia.backend.repository.UserRepository;

/**
 * Testes para o serviço de recuperação de senha.
 */
@ExtendWith(MockitoExtension.class)
class PasswordResetServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordResetTokenRepository tokenRepository;

    @Mock
    private PasswordHistoryRepository historyRepository;

    @Mock
    private PasswordResetRateLimitService rateLimitService;

    @Mock
    private SessionService sessionService;

    @Mock
    private EmailService emailService;

    @Mock
    private BCryptPasswordEncoder passwordEncoder;

    @Mock
    private PasswordResetProperties properties;

    @Mock
    private PasswordResetProperties.TokenConfig tokenConfig;

    @Mock
    private PasswordResetProperties.HistoryConfig historyConfig;

    @Mock
    private PasswordResetProperties.RateLimitConfig rateLimitConfig;

    @InjectMocks
    private PasswordResetService passwordResetService;

    private User testUser;
    private PasswordResetToken testToken;
    private String testEmail;
    private String testIpAddress;
    private String plainToken;

    @BeforeEach
    void setUp() {
        testEmail = "test@example.com";
        testIpAddress = "192.168.1.1";
        plainToken = "valid-plain-token";

        Profile studentProfile = Profile.builder()
                .id(UUID.randomUUID())
                .code(UserProfile.STUDENT)
                .name("Student")
                .hierarchyLevel(1)
                .active(true)
                .build();

        testUser = User.builder()
                .id(UUID.randomUUID())
                .name("Test User")
                .email(testEmail)
                .password("$2a$12$hashedPassword")
                .profile(studentProfile)
                .build();

        testToken = PasswordResetToken.builder()
                .id(UUID.randomUUID())
                .user(testUser)
                .tokenHash("hashed-token")
                .expiresAt(LocalDateTime.now().plusMinutes(30))
                .build();

        ReflectionTestUtils.setField(passwordResetService, "frontendUrl", "http://localhost:5173");
    }

    // ========== Testes de Solicitação de Reset ==========

    @Test
    void deveRetornarTrueQuandoEmailNaoExiste() {
        // Arrange
        when(rateLimitService.isEmailLimitReached(testEmail)).thenReturn(false);
        when(rateLimitService.isIpBlocked(testIpAddress)).thenReturn(false);
        when(userRepository.findByEmail(testEmail)).thenReturn(Optional.empty());

        // Act
        boolean result = passwordResetService.requestPasswordReset(testEmail, testIpAddress);

        // Assert
        assertTrue(result);
        verify(tokenRepository, never()).save(any(PasswordResetToken.class));
        verify(emailService, never()).sendPasswordResetEmail(anyString(), anyString(), anyString(), anyString());
    }

    @Test
    void deveEnviarEmailQuandoUsuarioExiste() {
        // Arrange
        when(rateLimitService.isEmailLimitReached(testEmail)).thenReturn(false);
        when(rateLimitService.isIpBlocked(testIpAddress)).thenReturn(false);
        when(userRepository.findByEmail(testEmail)).thenReturn(Optional.of(testUser));
        when(tokenRepository.invalidateAllUserTokens(any(UUID.class), any(LocalDateTime.class))).thenReturn(0);
        when(properties.getToken()).thenReturn(tokenConfig);
        when(tokenConfig.getExpirationMinutes()).thenReturn(30);
        when(tokenRepository.save(any(PasswordResetToken.class))).thenReturn(testToken);

        // Act
        boolean result = passwordResetService.requestPasswordReset(testEmail, testIpAddress);

        // Assert
        assertTrue(result);
        verify(tokenRepository).save(any(PasswordResetToken.class));
        verify(emailService).sendPasswordResetEmail(eq(testEmail), eq(testUser.getName()), anyString(), anyString());
    }

    @Test
    void deveRetornarTrueQuandoEmailLimitAtingido() {
        // Arrange
        when(rateLimitService.isEmailLimitReached(testEmail)).thenReturn(true);

        // Act
        boolean result = passwordResetService.requestPasswordReset(testEmail, testIpAddress);

        // Assert
        assertTrue(result);
        verify(rateLimitService, never()).isIpBlocked(anyString());
        verify(userRepository, never()).findByEmail(anyString());
    }

    @Test
    void deveLancarExcecaoQuandoIpBloqueado() {
        // Arrange
        when(rateLimitService.isEmailLimitReached(testEmail)).thenReturn(false);
        when(rateLimitService.isIpBlocked(testIpAddress)).thenReturn(true);
        when(rateLimitService.getTimeUntilReset(testIpAddress)).thenReturn(3600L);

        // Act & Assert
        RateLimitExceededException exception = assertThrows(RateLimitExceededException.class, () -> {
            passwordResetService.requestPasswordReset(testEmail, testIpAddress);
        });

        assertNotNull(exception);
        verify(userRepository, never()).findByEmail(anyString());
    }

    // ========== Testes de Validação de Token ==========

    @Test
    void deveRetornarInvalidoParaTokenNulo() {
        // Act
        ValidateTokenResponseDTO result = passwordResetService.validateToken(null);

        // Assert
        assertFalse(result.isValid());
    }

    @Test
    void deveRetornarInvalidoParaTokenVazio() {
        // Act
        ValidateTokenResponseDTO result = passwordResetService.validateToken("");

        // Assert
        assertFalse(result.isValid());
    }

    @Test
    void deveRetornarInvalidoParaTokenNaoEncontrado() {
        // Arrange
        when(tokenRepository.findByTokenHash(anyString())).thenReturn(Optional.empty());

        // Act
        ValidateTokenResponseDTO result = passwordResetService.validateToken(plainToken);

        // Assert
        assertFalse(result.isValid());
    }

    @Test
    void deveRetornarInvalidoParaTokenJaUsado() {
        // Arrange
        testToken.setUsedAt(LocalDateTime.now());
        when(tokenRepository.findByTokenHash(anyString())).thenReturn(Optional.of(testToken));

        // Act
        ValidateTokenResponseDTO result = passwordResetService.validateToken(plainToken);

        // Assert
        assertFalse(result.isValid());
        assertEquals("TOKEN_ALREADY_USED", result.getError());
    }

    @Test
    void deveRetornarInvalidoParaTokenExpirado() {
        // Arrange
        testToken.setExpiresAt(LocalDateTime.now().minusMinutes(1));
        when(tokenRepository.findByTokenHash(anyString())).thenReturn(Optional.of(testToken));

        // Act
        ValidateTokenResponseDTO result = passwordResetService.validateToken(plainToken);

        // Assert
        assertFalse(result.isValid());
        assertEquals("TOKEN_EXPIRED", result.getError());
    }

    @Test
    void deveRetornarValidoParaTokenValido() {
        // Arrange
        when(tokenRepository.findByTokenHash(anyString())).thenReturn(Optional.of(testToken));

        // Act
        ValidateTokenResponseDTO result = passwordResetService.validateToken(plainToken);

        // Assert
        assertTrue(result.isValid());
        assertNotNull(result.getEmail());
    }

    // ========== Testes de Redefinição de Senha ==========

    @Test
    void deveLancarExcecaoParaTokenInvalidoNoReset() {
        // Arrange
        when(tokenRepository.findByTokenHash(anyString())).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(InvalidResetTokenException.class, () -> {
            passwordResetService.resetPassword(plainToken, "NewPass123!");
        });
    }

    @Test
    void deveLancarExcecaoParaTokenExpiradoNoReset() {
        // Arrange
        testToken.setExpiresAt(LocalDateTime.now().minusMinutes(1));
        when(tokenRepository.findByTokenHash(anyString())).thenReturn(Optional.of(testToken));

        // Act & Assert
        assertThrows(InvalidResetTokenException.class, () -> {
            passwordResetService.resetPassword(plainToken, "NewPass123!");
        });
    }

    @Test
    void deveLancarExcecaoParaSenhaFraca() {
        // Arrange
        when(tokenRepository.findByTokenHash(anyString())).thenReturn(Optional.of(testToken));

        // Act & Assert
        PasswordValidationException exception = assertThrows(PasswordValidationException.class, () -> {
            passwordResetService.resetPassword(plainToken, "weak");
        });

        assertNotNull(exception.getMessage());
    }

    @Test
    void deveLancarExcecaoParaSenhaRecente() {
        // Arrange
        String newPassword = "NewPass123!";
        when(tokenRepository.findByTokenHash(anyString())).thenReturn(Optional.of(testToken));
        when(properties.getHistory()).thenReturn(historyConfig);
        when(historyConfig.getCount()).thenReturn(5);
        when(historyRepository.findLastPasswords(any(UUID.class), anyInt())).thenReturn(List.of());
        when(passwordEncoder.matches(newPassword, testUser.getPassword())).thenReturn(true);

        // Act & Assert
        assertThrows(PasswordRecentlyUsedException.class, () -> {
            passwordResetService.resetPassword(plainToken, newPassword);
        });
    }

    @Test
    void deveResetarSenhaComSucesso() {
        // Arrange
        String newPassword = "NewPass123!";
        when(tokenRepository.findByTokenHash(anyString())).thenReturn(Optional.of(testToken));
        when(properties.getHistory()).thenReturn(historyConfig);
        when(historyConfig.getCount()).thenReturn(5);
        when(historyRepository.findLastPasswords(any(UUID.class), anyInt())).thenReturn(List.of());
        when(passwordEncoder.matches(newPassword, testUser.getPassword())).thenReturn(false);
        when(passwordEncoder.encode(newPassword)).thenReturn("$2a$12$newHashedPassword");
        when(sessionService.removeAllUserSessions(anyString())).thenReturn(1L);

        // Act
        boolean result = passwordResetService.resetPassword(plainToken, newPassword);

        // Assert
        assertTrue(result);
        verify(userRepository).save(testUser);
        verify(tokenRepository).save(testToken);
        verify(historyRepository).save(any(PasswordHistory.class));
        verify(sessionService).removeAllUserSessions(testUser.getId().toString());
        verify(emailService).sendPasswordChangedEmail(testUser.getEmail(), testUser.getName());
    }

    // ========== Testes de Validação de Complexidade de Senha ==========

    @Test
    void deveRetornarErroParaSenhaNula() {
        List<String> errors = passwordResetService.validatePasswordComplexity(null);
        assertFalse(errors.isEmpty());
    }

    @Test
    void deveRetornarErroParaSenhaCurta() {
        List<String> errors = passwordResetService.validatePasswordComplexity("Aa1!");
        assertTrue(errors.stream().anyMatch(e -> e.contains("8 caracteres")));
    }

    @Test
    void deveRetornarErroParaSenhaSemMaiuscula() {
        List<String> errors = passwordResetService.validatePasswordComplexity("abcd1234!");
        assertTrue(errors.stream().anyMatch(e -> e.contains("maiúscula")));
    }

    @Test
    void deveRetornarErroParaSenhaSemMinuscula() {
        List<String> errors = passwordResetService.validatePasswordComplexity("ABCD1234!");
        assertTrue(errors.stream().anyMatch(e -> e.contains("minúscula")));
    }

    @Test
    void deveRetornarErroParaSenhaSemNumero() {
        List<String> errors = passwordResetService.validatePasswordComplexity("AbcdEfgh!");
        assertTrue(errors.stream().anyMatch(e -> e.contains("número")));
    }

    @Test
    void deveRetornarErroParaSenhaSemCaractereEspecial() {
        List<String> errors = passwordResetService.validatePasswordComplexity("AbcdEfgh1");
        assertTrue(errors.stream().anyMatch(e -> e.contains("especial")));
    }

    @Test
    void deveRetornarListaVaziaParaSenhaValida() {
        List<String> errors = passwordResetService.validatePasswordComplexity("AbcdEfgh1!");
        assertTrue(errors.isEmpty());
    }
}
