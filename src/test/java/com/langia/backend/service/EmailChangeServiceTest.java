package com.langia.backend.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import com.langia.backend.exception.EmailAlreadyExistsException;
import com.langia.backend.exception.InvalidEmailChangeCodeException;
import com.langia.backend.exception.UserNotFoundException;
import com.langia.backend.model.EmailChangeRequest;
import com.langia.backend.model.Profile;
import com.langia.backend.model.User;
import com.langia.backend.model.UserProfile;
import com.langia.backend.repository.EmailChangeRequestRepository;
import com.langia.backend.repository.UserRepository;

/**
 * Testes unitarios para o servico de alteracao de e-mail.
 */
@ExtendWith(MockitoExtension.class)
class EmailChangeServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private EmailChangeRequestRepository requestRepository;

    @Mock
    private EmailService emailService;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private EmailChangeService emailChangeService;

    private User testUser;
    private EmailChangeRequest validRequest;
    private UUID userId;
    private String newEmail;
    private String validCode;

    @BeforeEach
    void setUp() {
        userId = UUID.randomUUID();
        newEmail = "newemail@example.com";
        validCode = "123456";

        // Cria perfil de teste
        Profile studentProfile = Profile.builder()
                .id(UUID.randomUUID())
                .code(UserProfile.STUDENT)
                .name("Student")
                .hierarchyLevel(1)
                .active(true)
                .build();

        testUser = User.builder()
                .id(userId)
                .name("Test User")
                .email("oldemail@example.com")
                .password("$2a$12$hashedPassword")
                .profile(studentProfile)
                .emailVerified(true)
                .build();

        validRequest = EmailChangeRequest.builder()
                .id(UUID.randomUUID())
                .user(testUser)
                .newEmail(newEmail)
                .tokenHash("$2a$12$hashedCode")
                .expiresAt(LocalDateTime.now().plusMinutes(15))
                .build();
    }

    // ========== Testes de Solicitacao de Alteracao ==========

    @Test
    void deveEnviarSolicitacaoDeAlteracaoComSucesso() {
        // Arrange
        when(userRepository.findById(userId)).thenReturn(Optional.of(testUser));
        when(userRepository.existsByEmail(newEmail)).thenReturn(false);
        when(requestRepository.invalidateAllUserRequests(any(UUID.class), any(LocalDateTime.class))).thenReturn(0);
        when(passwordEncoder.encode(anyString())).thenReturn("$2a$12$hashedCode");
        when(requestRepository.save(any(EmailChangeRequest.class))).thenReturn(validRequest);

        // Act
        emailChangeService.requestEmailChange(userId, newEmail);

        // Assert
        verify(requestRepository).invalidateAllUserRequests(eq(userId), any(LocalDateTime.class));
        verify(requestRepository).save(any(EmailChangeRequest.class));
        verify(emailService).sendEmailChangeVerification(eq(newEmail), eq(testUser.getName()), anyString());
    }

    @Test
    void deveRejeitarSolicitacaoComEmailJaExistente() {
        // Arrange
        when(userRepository.findById(userId)).thenReturn(Optional.of(testUser));
        when(userRepository.existsByEmail(newEmail)).thenReturn(true);

        // Act & Assert
        assertThrows(
                EmailAlreadyExistsException.class,
                () -> emailChangeService.requestEmailChange(userId, newEmail)
        );

        verify(requestRepository, never()).save(any(EmailChangeRequest.class));
        verify(emailService, never()).sendEmailChangeVerification(anyString(), anyString(), anyString());
    }

    @Test
    void deveRejeitarSolicitacaoParaUsuarioInexistente() {
        // Arrange
        UUID nonExistentUserId = UUID.randomUUID();
        when(userRepository.findById(nonExistentUserId)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(
                UserNotFoundException.class,
                () -> emailChangeService.requestEmailChange(nonExistentUserId, newEmail)
        );

        verify(requestRepository, never()).save(any(EmailChangeRequest.class));
    }

    // ========== Testes de Confirmacao de Alteracao ==========

    @Test
    void deveConfirmarAlteracaoDeEmailComSucesso() {
        // Arrange
        when(userRepository.findById(userId)).thenReturn(Optional.of(testUser));
        when(requestRepository.findActiveRequestsByUserId(eq(userId), any(LocalDateTime.class)))
                .thenReturn(List.of(validRequest));
        when(passwordEncoder.matches(validCode, validRequest.getTokenHash())).thenReturn(true);
        when(userRepository.existsByEmail(newEmail)).thenReturn(false);
        when(userRepository.save(any(User.class))).thenReturn(testUser);
        when(requestRepository.save(any(EmailChangeRequest.class))).thenReturn(validRequest);

        // Act
        emailChangeService.confirmEmailChange(userId, validCode);

        // Assert
        // Verifica que o usuario foi atualizado com o novo email
        ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);
        verify(userRepository).save(userCaptor.capture());
        assertEquals(newEmail, userCaptor.getValue().getEmail());

        // Verifica que a solicitacao foi marcada como usada
        ArgumentCaptor<EmailChangeRequest> requestCaptor = ArgumentCaptor.forClass(EmailChangeRequest.class);
        verify(requestRepository).save(requestCaptor.capture());
        assertNotNull(requestCaptor.getValue().getUsedAt());

        // Verifica que o email de notificacao foi enviado
        verify(emailService).sendEmailChangedNotification(
                eq("oldemail@example.com"),
                eq(testUser.getName()),
                eq(newEmail)
        );
    }

    @Test
    void deveRejeitarConfirmacaoComCodigoInvalido() {
        // Arrange
        String invalidCode = "999999";
        when(userRepository.findById(userId)).thenReturn(Optional.of(testUser));
        when(requestRepository.findActiveRequestsByUserId(eq(userId), any(LocalDateTime.class)))
                .thenReturn(List.of(validRequest));
        when(passwordEncoder.matches(invalidCode, validRequest.getTokenHash())).thenReturn(false);

        // Act & Assert
        assertThrows(
                InvalidEmailChangeCodeException.class,
                () -> emailChangeService.confirmEmailChange(userId, invalidCode)
        );

        verify(userRepository, never()).save(any(User.class));
        verify(emailService, never()).sendEmailChangedNotification(anyString(), anyString(), anyString());
    }

    @Test
    void deveRejeitarConfirmacaoSemSolicitacoesAtivas() {
        // Arrange
        when(userRepository.findById(userId)).thenReturn(Optional.of(testUser));
        when(requestRepository.findActiveRequestsByUserId(eq(userId), any(LocalDateTime.class)))
                .thenReturn(Collections.emptyList());

        // Act & Assert
        assertThrows(
                InvalidEmailChangeCodeException.class,
                () -> emailChangeService.confirmEmailChange(userId, validCode)
        );

        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void deveRejeitarConfirmacaoQuandoEmailNaoMaisDisponivel() {
        // Arrange
        when(userRepository.findById(userId)).thenReturn(Optional.of(testUser));
        when(requestRepository.findActiveRequestsByUserId(eq(userId), any(LocalDateTime.class)))
                .thenReturn(List.of(validRequest));
        when(passwordEncoder.matches(validCode, validRequest.getTokenHash())).thenReturn(true);
        // Simula que outro usuario pegou o email durante o processo
        when(userRepository.existsByEmail(newEmail)).thenReturn(true);
        when(requestRepository.save(any(EmailChangeRequest.class))).thenReturn(validRequest);

        // Act & Assert
        assertThrows(
                EmailAlreadyExistsException.class,
                () -> emailChangeService.confirmEmailChange(userId, validCode)
        );

        // Verifica que a solicitacao foi invalidada
        verify(requestRepository).save(any(EmailChangeRequest.class));

        // Verifica que o email do usuario NAO foi alterado
        verify(userRepository, never()).save(any(User.class));
        verify(emailService, never()).sendEmailChangedNotification(anyString(), anyString(), anyString());
    }

    @Test
    void deveRejeitarConfirmacaoParaUsuarioInexistente() {
        // Arrange
        UUID nonExistentUserId = UUID.randomUUID();
        when(userRepository.findById(nonExistentUserId)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(
                UserNotFoundException.class,
                () -> emailChangeService.confirmEmailChange(nonExistentUserId, validCode)
        );

        verify(requestRepository, never()).findActiveRequestsByUserId(any(UUID.class), any(LocalDateTime.class));
        verify(emailService, never()).sendEmailChangedNotification(anyString(), anyString(), anyString());
    }

    @Test
    void deveUsarQueryOtimizadaParaBuscarSolicitacoesAtivas() {
        // Arrange
        when(userRepository.findById(userId)).thenReturn(Optional.of(testUser));
        when(requestRepository.findActiveRequestsByUserId(eq(userId), any(LocalDateTime.class)))
                .thenReturn(List.of(validRequest));
        when(passwordEncoder.matches(validCode, validRequest.getTokenHash())).thenReturn(true);
        when(userRepository.existsByEmail(newEmail)).thenReturn(false);
        when(userRepository.save(any(User.class))).thenReturn(testUser);
        when(requestRepository.save(any(EmailChangeRequest.class))).thenReturn(validRequest);

        // Act
        emailChangeService.confirmEmailChange(userId, validCode);

        // Assert - Verifica que usou o metodo otimizado e nao findAll
        verify(requestRepository).findActiveRequestsByUserId(eq(userId), any(LocalDateTime.class));
        verify(requestRepository, never()).findAll();
    }
}
