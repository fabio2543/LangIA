package com.langia.backend.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import com.langia.backend.dto.EmailVerificationResponseDTO;
import com.langia.backend.dto.ResendVerificationResponseDTO;
import com.langia.backend.exception.EmailVerificationRateLimitException;
import com.langia.backend.exception.InvalidVerificationTokenException;
import com.langia.backend.model.EmailVerificationToken;
import com.langia.backend.model.User;
import com.langia.backend.model.UserProfile;
import com.langia.backend.repository.EmailVerificationTokenRepository;
import com.langia.backend.repository.UserRepository;
import com.langia.backend.util.TokenHashUtil;

/**
 * Testes para o servico de verificacao de e-mail.
 */
@ExtendWith(MockitoExtension.class)
class EmailVerificationServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private EmailVerificationTokenRepository tokenRepository;

    @Mock
    private EmailVerificationRateLimitService rateLimitService;

    @Mock
    private EmailService emailService;

    @InjectMocks
    private EmailVerificationService verificationService;

    private User testUser;
    private EmailVerificationToken validToken;
    private EmailVerificationToken expiredToken;
    private EmailVerificationToken usedToken;
    private String plainToken;

    @BeforeEach
    void setUp() {
        // Configuracao do usuario de teste
        testUser = User.builder()
                .id(UUID.randomUUID())
                .name("Test User")
                .email("test@example.com")
                .password("$2a$12$hashedPassword")
                .profile(UserProfile.STUDENT)
                .cpfString("11144477735")
                .phone("11987654321")
                .emailVerified(false)
                .build();

        // Token em texto plano
        plainToken = TokenHashUtil.generateSecureToken();
        String tokenHash = TokenHashUtil.hashToken(plainToken);

        // Token valido
        validToken = EmailVerificationToken.builder()
                .id(UUID.randomUUID())
                .user(testUser)
                .tokenHash(tokenHash)
                .expiresAt(LocalDateTime.now().plusHours(24))
                .build();

        // Token expirado
        expiredToken = EmailVerificationToken.builder()
                .id(UUID.randomUUID())
                .user(testUser)
                .tokenHash(tokenHash)
                .expiresAt(LocalDateTime.now().minusHours(1))
                .build();

        // Token ja usado
        usedToken = EmailVerificationToken.builder()
                .id(UUID.randomUUID())
                .user(testUser)
                .tokenHash(tokenHash)
                .expiresAt(LocalDateTime.now().plusHours(24))
                .usedAt(LocalDateTime.now().minusMinutes(30))
                .build();

        // Configura valores via reflection
        ReflectionTestUtils.setField(verificationService, "frontendUrl", "http://localhost:5173");
        ReflectionTestUtils.setField(verificationService, "tokenExpirationHours", 24);
    }

    // ========== Testes de Envio de Verificacao ==========

    @Test
    void deveEnviarEmailDeVerificacaoComSucesso() {
        // Arrange
        when(tokenRepository.invalidateAllUserTokens(any(UUID.class), any(LocalDateTime.class))).thenReturn(0);
        when(tokenRepository.save(any(EmailVerificationToken.class))).thenReturn(validToken);

        // Act
        verificationService.sendVerificationEmail(testUser);

        // Assert
        verify(tokenRepository).invalidateAllUserTokens(eq(testUser.getId()), any(LocalDateTime.class));
        verify(tokenRepository).save(any(EmailVerificationToken.class));
        verify(emailService).sendEmailVerificationEmail(
                eq(testUser.getEmail()),
                eq(testUser.getName()),
                anyString(),
                eq("24 horas")
        );
    }

    @Test
    void deveInvalidarTokensAnterioresAoEnviarNovoEmail() {
        // Arrange
        when(tokenRepository.invalidateAllUserTokens(any(UUID.class), any(LocalDateTime.class))).thenReturn(2);
        when(tokenRepository.save(any(EmailVerificationToken.class))).thenReturn(validToken);

        // Act
        verificationService.sendVerificationEmail(testUser);

        // Assert
        verify(tokenRepository).invalidateAllUserTokens(eq(testUser.getId()), any(LocalDateTime.class));
    }

    // ========== Testes de Confirmacao de E-mail ==========

    @Test
    void deveConfirmarEmailComTokenValido() {
        // Arrange
        String tokenHash = TokenHashUtil.hashToken(plainToken);
        validToken.setTokenHash(tokenHash);

        when(tokenRepository.findByTokenHash(tokenHash)).thenReturn(Optional.of(validToken));
        when(tokenRepository.save(any(EmailVerificationToken.class))).thenReturn(validToken);
        when(userRepository.save(any(User.class))).thenReturn(testUser);

        // Act
        EmailVerificationResponseDTO response = verificationService.confirmEmail(plainToken);

        // Assert
        assertTrue(response.isSuccess());
        assertNotNull(response.getMessage());

        // Verifica que o token foi marcado como usado
        ArgumentCaptor<EmailVerificationToken> tokenCaptor = ArgumentCaptor.forClass(EmailVerificationToken.class);
        verify(tokenRepository).save(tokenCaptor.capture());
        assertNotNull(tokenCaptor.getValue().getUsedAt());

        // Verifica que o usuario foi atualizado
        ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);
        verify(userRepository).save(userCaptor.capture());
        assertTrue(userCaptor.getValue().isEmailVerified());
        assertNotNull(userCaptor.getValue().getEmailVerifiedAt());
    }

    @Test
    void deveRejeitarTokenInvalido() {
        // Arrange
        String invalidToken = "invalid-token-123";
        String tokenHash = TokenHashUtil.hashToken(invalidToken);

        when(tokenRepository.findByTokenHash(tokenHash)).thenReturn(Optional.empty());

        // Act & Assert
        InvalidVerificationTokenException exception = assertThrows(
                InvalidVerificationTokenException.class,
                () -> verificationService.confirmEmail(invalidToken)
        );

        assertEquals("TOKEN_INVALID", exception.getErrorCode());
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void deveRejeitarTokenExpirado() {
        // Arrange
        String tokenHash = TokenHashUtil.hashToken(plainToken);
        expiredToken.setTokenHash(tokenHash);

        when(tokenRepository.findByTokenHash(tokenHash)).thenReturn(Optional.of(expiredToken));

        // Act & Assert
        InvalidVerificationTokenException exception = assertThrows(
                InvalidVerificationTokenException.class,
                () -> verificationService.confirmEmail(plainToken)
        );

        assertEquals("TOKEN_EXPIRED", exception.getErrorCode());
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void deveRejeitarTokenJaUtilizado() {
        // Arrange
        String tokenHash = TokenHashUtil.hashToken(plainToken);
        usedToken.setTokenHash(tokenHash);

        when(tokenRepository.findByTokenHash(tokenHash)).thenReturn(Optional.of(usedToken));

        // Act & Assert
        InvalidVerificationTokenException exception = assertThrows(
                InvalidVerificationTokenException.class,
                () -> verificationService.confirmEmail(plainToken)
        );

        assertEquals("TOKEN_USED", exception.getErrorCode());
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void deveRejeitarTokenNuloOuVazio() {
        // Act & Assert - Token nulo
        assertThrows(InvalidVerificationTokenException.class,
                () -> verificationService.confirmEmail(null));

        // Act & Assert - Token vazio
        assertThrows(InvalidVerificationTokenException.class,
                () -> verificationService.confirmEmail(""));

        // Act & Assert - Token apenas espacos
        assertThrows(InvalidVerificationTokenException.class,
                () -> verificationService.confirmEmail("   "));

        verify(tokenRepository, never()).findByTokenHash(anyString());
    }

    // ========== Testes de Reenvio de Verificacao ==========

    @Test
    void deveReenviarVerificacaoComSucesso() {
        // Arrange
        when(userRepository.findById(testUser.getId())).thenReturn(Optional.of(testUser));
        when(rateLimitService.isResendLimitReached(testUser.getId())).thenReturn(false);
        when(rateLimitService.getRemainingResends(testUser.getId())).thenReturn(2);
        when(tokenRepository.invalidateAllUserTokens(any(UUID.class), any(LocalDateTime.class))).thenReturn(0);
        when(tokenRepository.save(any(EmailVerificationToken.class))).thenReturn(validToken);

        // Act
        ResendVerificationResponseDTO response = verificationService.resendVerification(testUser.getId());

        // Assert
        assertTrue(response.isSuccess());
        assertEquals(2, response.getRemainingResends());

        verify(rateLimitService).recordResendAttempt(testUser.getId());
        verify(emailService).sendEmailVerificationEmail(anyString(), anyString(), anyString(), anyString());
    }

    @Test
    void deveRetornarJaVerificadoParaUsuarioVerificado() {
        // Arrange
        testUser.setEmailVerified(true);
        when(userRepository.findById(testUser.getId())).thenReturn(Optional.of(testUser));

        // Act
        ResendVerificationResponseDTO response = verificationService.resendVerification(testUser.getId());

        // Assert
        assertFalse(response.isSuccess());
        assertEquals("E-mail ja verificado.", response.getMessage());

        verify(rateLimitService, never()).recordResendAttempt(any());
        verify(emailService, never()).sendEmailVerificationEmail(anyString(), anyString(), anyString(), anyString());
    }

    @Test
    void deveRejeitarReenvioQuandoRateLimitExcedido() {
        // Arrange
        when(userRepository.findById(testUser.getId())).thenReturn(Optional.of(testUser));
        when(rateLimitService.isResendLimitReached(testUser.getId())).thenReturn(true);
        when(rateLimitService.getTimeUntilReset(testUser.getId())).thenReturn(1800L);

        // Act & Assert
        EmailVerificationRateLimitException exception = assertThrows(
                EmailVerificationRateLimitException.class,
                () -> verificationService.resendVerification(testUser.getId())
        );

        assertEquals(1800L, exception.getRetryAfterSeconds());
        verify(emailService, never()).sendEmailVerificationEmail(anyString(), anyString(), anyString(), anyString());
    }

    @Test
    void deveRejeitarReenvioParaUsuarioInexistente() {
        // Arrange
        UUID nonExistentUserId = UUID.randomUUID();
        when(userRepository.findById(nonExistentUserId)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(RuntimeException.class,
                () -> verificationService.resendVerification(nonExistentUserId));

        verify(emailService, never()).sendEmailVerificationEmail(anyString(), anyString(), anyString(), anyString());
    }

    // ========== Testes de Verificacao de Status ==========

    @Test
    void deveRetornarTrueParaEmailVerificado() {
        // Arrange
        testUser.setEmailVerified(true);
        when(userRepository.findById(testUser.getId())).thenReturn(Optional.of(testUser));

        // Act
        boolean result = verificationService.isEmailVerified(testUser.getId());

        // Assert
        assertTrue(result);
    }

    @Test
    void deveRetornarFalseParaEmailNaoVerificado() {
        // Arrange
        testUser.setEmailVerified(false);
        when(userRepository.findById(testUser.getId())).thenReturn(Optional.of(testUser));

        // Act
        boolean result = verificationService.isEmailVerified(testUser.getId());

        // Assert
        assertFalse(result);
    }

    @Test
    void deveRetornarFalseParaUsuarioInexistente() {
        // Arrange
        UUID nonExistentUserId = UUID.randomUUID();
        when(userRepository.findById(nonExistentUserId)).thenReturn(Optional.empty());

        // Act
        boolean result = verificationService.isEmailVerified(nonExistentUserId);

        // Assert
        assertFalse(result);
    }
}
