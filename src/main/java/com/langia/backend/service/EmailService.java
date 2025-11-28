package com.langia.backend.service;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import com.resend.Resend;
import com.resend.core.exception.ResendException;
import com.resend.services.emails.model.CreateEmailOptions;
import com.resend.services.emails.model.CreateEmailResponse;

import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * Serviço para envio de emails utilizando Resend API e templates Thymeleaf.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class EmailService {

    private final TemplateEngine templateEngine;

    @Value("${resend.api-key:}")
    private String resendApiKey;

    @Value("${resend.from-email:onboarding@resend.dev}")
    private String fromEmail;

    @Value("${app.platform.name:LangIA}")
    private String platformName;

    @Value("${app.support.email:suporte@langia.com}")
    private String supportEmail;

    private Resend resend;

    private static final DateTimeFormatter DATE_TIME_FORMATTER =
            DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    @PostConstruct
    public void init() {
        if (resendApiKey != null && !resendApiKey.isBlank()) {
            this.resend = new Resend(resendApiKey);
            log.info("Resend email service initialized successfully");
        } else {
            log.warn("Resend API key not configured - emails will not be sent");
        }
    }

    /**
     * Envia email de recuperação de senha.
     *
     * @param toEmail        Email do destinatário
     * @param userName       Nome do usuário
     * @param resetLink      Link para redefinição de senha
     * @param expirationTime Tempo de expiração do link
     */
    public void sendPasswordResetEmail(String toEmail, String userName, String resetLink, String expirationTime) {
        log.info("Preparing password reset email for: {}", maskEmail(toEmail));

        Context context = new Context();
        context.setVariable("userName", userName);
        context.setVariable("resetLink", resetLink);
        context.setVariable("expirationTime", expirationTime);
        context.setVariable("platformName", platformName);

        String html = templateEngine.process("email/password-reset-email", context);
        String subject = "Recuperação de Senha - " + platformName;

        sendHtmlEmail(toEmail, subject, html);
    }

    /**
     * Envia email de confirmação de alteração de senha.
     *
     * @param toEmail  Email do destinatário
     * @param userName Nome do usuário
     */
    public void sendPasswordChangedEmail(String toEmail, String userName) {
        log.info("Preparing password changed email for: {}", maskEmail(toEmail));

        Context context = new Context();
        context.setVariable("userName", userName);
        context.setVariable("changeTime", LocalDateTime.now().format(DATE_TIME_FORMATTER));
        context.setVariable("platformName", platformName);
        context.setVariable("supportEmail", supportEmail);

        String html = templateEngine.process("email/password-changed-email", context);
        String subject = "Senha Alterada - " + platformName;

        sendHtmlEmail(toEmail, subject, html);
    }

    /**
     * Envia email de verificacao de e-mail.
     *
     * @param toEmail        Email do destinatario
     * @param userName       Nome do usuario
     * @param verifyLink     Link para verificacao
     * @param expirationTime Tempo de expiracao do link
     */
    public void sendEmailVerificationEmail(String toEmail, String userName, String verifyLink, String expirationTime) {
        log.info("Preparing email verification for: {}", maskEmail(toEmail));

        Context context = new Context();
        context.setVariable("userName", userName);
        context.setVariable("verifyLink", verifyLink);
        context.setVariable("expirationTime", expirationTime);
        context.setVariable("platformName", platformName);
        context.setVariable("supportEmail", supportEmail);

        String html = templateEngine.process("email/email-verification", context);
        String subject = "Confirme seu e-mail - " + platformName;

        sendHtmlEmail(toEmail, subject, html);
    }

    /**
     * Envia email HTML usando Resend API.
     *
     * @param to      Destinatario
     * @param subject Assunto
     * @param html    Conteudo HTML
     */
    private void sendHtmlEmail(String to, String subject, String html) {
        if (resend == null) {
            log.warn("Email not sent - Resend not configured. To: {}, Subject: {}", maskEmail(to), subject);
            return;
        }

        try {
            CreateEmailOptions options = CreateEmailOptions.builder()
                    .from(platformName + " <" + fromEmail + ">")
                    .to(to)
                    .subject(subject)
                    .html(html)
                    .build();

            CreateEmailResponse response = resend.emails().send(options);
            log.info("Email sent successfully to: {} - ID: {}", maskEmail(to), response.getId());

        } catch (ResendException e) {
            log.error("Failed to send email to: {} - Error: {}", maskEmail(to), e.getMessage());
            // Não lançamos exceção para não bloquear o fluxo de recuperação de senha
            // O usuário receberá resposta de sucesso mesmo que o email falhe
        }
    }

    /**
     * Mascara o email para logs seguros.
     */
    private String maskEmail(String email) {
        if (email == null || !email.contains("@")) {
            return "***";
        }
        String[] parts = email.split("@");
        String localPart = parts[0];
        String domain = parts[1];

        String maskedLocal = localPart.length() > 2
                ? localPart.charAt(0) + "***" + localPart.charAt(localPart.length() - 1)
                : "***";

        String[] domainParts = domain.split("\\.");
        String maskedDomain = domainParts[0].length() > 2
                ? domainParts[0].charAt(0) + "***"
                : "***";

        return maskedLocal + "@" + maskedDomain + "." + (domainParts.length > 1 ? domainParts[domainParts.length - 1] : "com");
    }
}
