package com.langia.backend.service;

import org.springframework.stereotype.Service;

import lombok.extern.slf4j.Slf4j;

/**
 * Serviço para envio de emails.
 *
 * TODO: Implementar integração com provedor de email (SendGrid, SES, etc.)
 * Por enquanto, apenas loga as operações.
 */
@Service
@Slf4j
public class EmailService {

    /**
     * Envia email de recuperação de senha.
     *
     * @param toEmail Email do destinatário
     * @param userName Nome do usuário
     * @param resetLink Link para redefinição de senha
     * @param expirationTime Tempo de expiração do link
     */
    public void sendPasswordResetEmail(String toEmail, String userName, String resetLink, String expirationTime) {
        log.info("=== EMAIL DE RECUPERAÇÃO DE SENHA ===");
        log.info("Para: {}", toEmail);
        log.info("Nome: {}", userName);
        log.info("Link: {}", resetLink);
        log.info("Expira em: {}", expirationTime);
        log.info("=====================================");

        // TODO: Implementar envio real de email
        // Exemplo com Spring Mail:
        // MimeMessage message = mailSender.createMimeMessage();
        // MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
        // helper.setTo(toEmail);
        // helper.setSubject("Recuperação de Senha - LangIA");
        // helper.setText(buildPasswordResetEmailHtml(userName, resetLink, expirationTime), true);
        // mailSender.send(message);
    }

    /**
     * Envia email de confirmação de alteração de senha.
     *
     * @param toEmail Email do destinatário
     * @param userName Nome do usuário
     */
    public void sendPasswordChangedEmail(String toEmail, String userName) {
        log.info("=== EMAIL DE SENHA ALTERADA ===");
        log.info("Para: {}", toEmail);
        log.info("Nome: {}", userName);
        log.info("Mensagem: Sua senha foi alterada com sucesso.");
        log.info("===============================");

        // TODO: Implementar envio real de email
    }
}
