package com.langia.backend.service;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * Serviço para envio de emails utilizando templates Thymeleaf.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class EmailService {

    private final JavaMailSender mailSender;
    private final TemplateEngine templateEngine;

    @Value("${spring.mail.username:noreply@langia.com}")
    private String fromEmail;

    @Value("${app.platform.name:LangIA}")
    private String platformName;

    @Value("${app.support.email:suporte@langia.com}")
    private String supportEmail;

    private static final DateTimeFormatter DATE_TIME_FORMATTER =
            DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    /**
     * Envia email de recuperação de senha.
     *
     * @param toEmail        Email do destinatário
     * @param userName       Nome do usuário
     * @param resetLink      Link para redefinição de senha
     * @param expirationTime Tempo de expiração do link
     */
    public void sendPasswordResetEmail(String toEmail, String userName, String resetLink, String expirationTime) {
        log.info("Preparing password reset email for: {}", toEmail);

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
        log.info("Preparing password changed email for: {}", toEmail);

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
     * Envia email HTML.
     *
     * @param to      Destinatário
     * @param subject Assunto
     * @param html    Conteúdo HTML
     */
    private void sendHtmlEmail(String to, String subject, String html) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            helper.setFrom(fromEmail);
            helper.setTo(to);
            helper.setSubject(subject);
            helper.setText(html, true);

            mailSender.send(message);
            log.info("Email sent successfully to: {}", to);

        } catch (MessagingException e) {
            log.error("Failed to send email to: {} - Error: {}", to, e.getMessage());
            // Não lançamos exceção para não bloquear o fluxo de recuperação de senha
            // O usuário receberá resposta de sucesso mesmo que o email falhe
        }
    }
}
