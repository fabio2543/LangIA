package com.langia.backend.service;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

/**
 * Testes para o serviço de email.
 */
@ExtendWith(MockitoExtension.class)
class EmailServiceTest {

    @Mock
    private TemplateEngine templateEngine;

    @InjectMocks
    private EmailService emailService;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(emailService, "resendApiKey", "");
        ReflectionTestUtils.setField(emailService, "fromEmail", "test@test.com");
        ReflectionTestUtils.setField(emailService, "platformName", "LangIA");
        ReflectionTestUtils.setField(emailService, "supportEmail", "suporte@langia.com");
    }

    @Test
    void deveProcessarTemplateDePasswordReset() {
        // Arrange
        when(templateEngine.process(anyString(), org.mockito.ArgumentMatchers.any(Context.class)))
                .thenReturn("<html>Test</html>");

        // Act
        emailService.sendPasswordResetEmail("user@test.com", "User", "http://link", "30 minutos");

        // Assert
        verify(templateEngine).process(org.mockito.ArgumentMatchers.eq("email/password-reset-email"),
                org.mockito.ArgumentMatchers.any(Context.class));
    }

    @Test
    void deveProcessarTemplateDePasswordChanged() {
        // Arrange
        when(templateEngine.process(anyString(), org.mockito.ArgumentMatchers.any(Context.class)))
                .thenReturn("<html>Test</html>");

        // Act
        emailService.sendPasswordChangedEmail("user@test.com", "User");

        // Assert
        verify(templateEngine).process(org.mockito.ArgumentMatchers.eq("email/password-changed-email"),
                org.mockito.ArgumentMatchers.any(Context.class));
    }

    @Test
    void deveProcessarTemplateDeEmailVerification() {
        // Arrange
        when(templateEngine.process(anyString(), org.mockito.ArgumentMatchers.any(Context.class)))
                .thenReturn("<html>Test</html>");

        // Act
        emailService.sendEmailVerificationEmail("user@test.com", "User", "http://link", "24 horas");

        // Assert
        verify(templateEngine).process(org.mockito.ArgumentMatchers.eq("email/email-verification"),
                org.mockito.ArgumentMatchers.any(Context.class));
    }

    @Test
    void deveProcessarTemplateDeEmailChangeVerification() {
        // Arrange
        when(templateEngine.process(anyString(), org.mockito.ArgumentMatchers.any(Context.class)))
                .thenReturn("<html>Test</html>");

        // Act
        emailService.sendEmailChangeVerification("user@test.com", "User", "123456");

        // Assert
        verify(templateEngine).process(org.mockito.ArgumentMatchers.eq("email/email-change-verification"),
                org.mockito.ArgumentMatchers.any(Context.class));
    }

    @Test
    void deveProcessarTemplateDeEmailChangedNotification() {
        // Arrange
        when(templateEngine.process(anyString(), org.mockito.ArgumentMatchers.any(Context.class)))
                .thenReturn("<html>Test</html>");

        // Act
        emailService.sendEmailChangedNotification("old@test.com", "User", "new@test.com");

        // Assert
        verify(templateEngine).process(org.mockito.ArgumentMatchers.eq("email/email-changed-notification"),
                org.mockito.ArgumentMatchers.any(Context.class));
    }
}
