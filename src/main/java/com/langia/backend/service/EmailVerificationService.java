package com.langia.backend.service;

import java.time.LocalDateTime;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.langia.backend.dto.EmailVerificationResponseDTO;
import com.langia.backend.dto.ResendVerificationResponseDTO;
import com.langia.backend.exception.EmailVerificationRateLimitException;
import com.langia.backend.exception.InvalidVerificationTokenException;
import com.langia.backend.model.EmailVerificationToken;
import com.langia.backend.model.User;
import com.langia.backend.repository.EmailVerificationTokenRepository;
import com.langia.backend.repository.UserRepository;
import com.langia.backend.util.EmailMaskUtil;
import com.langia.backend.util.TokenHashUtil;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * Servico de negocio para verificacao de e-mail.
 * Implementa a logica de envio, reenvio e confirmacao de verificacao.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class EmailVerificationService {

    private final UserRepository userRepository;
    private final EmailVerificationTokenRepository tokenRepository;
    private final EmailVerificationRateLimitService rateLimitService;
    private final EmailService emailService;

    @Value("${app.frontend.url:http://localhost:5173}")
    private String frontendUrl;

    @Value("${email.verification.token.expiration-hours:24}")
    private int tokenExpirationHours;

    /**
     * Cria e envia token de verificacao para um usuario.
     *
     * @param user Usuario que precisa verificar o e-mail
     */
    @Transactional
    public void sendVerificationEmail(User user) {
        // 1. Invalidar tokens anteriores
        int invalidated = tokenRepository.invalidateAllUserTokens(user.getId(), LocalDateTime.now());
        if (invalidated > 0) {
            log.debug("Invalidated {} previous verification tokens for user: {}", invalidated, user.getId());
        }

        // 2. Gerar novo token
        String plainToken = TokenHashUtil.generateSecureToken();
        String tokenHash = TokenHashUtil.hashToken(plainToken);
        LocalDateTime expiresAt = LocalDateTime.now().plusHours(tokenExpirationHours);

        // 3. Salvar token
        EmailVerificationToken token = EmailVerificationToken.builder()
            .user(user)
            .tokenHash(tokenHash)
            .expiresAt(expiresAt)
            .build();
        tokenRepository.save(token);

        // 4. Enviar email
        String verifyLink = frontendUrl + "/email-confirmed?token=" + plainToken;
        String expirationTime = tokenExpirationHours + " horas";

        emailService.sendEmailVerificationEmail(
            user.getEmail(),
            user.getName(),
            verifyLink,
            expirationTime
        );

        log.info("Verification email sent to user: {}", user.getId());
    }

    /**
     * Reenvia e-mail de verificacao (com rate limiting).
     *
     * @param userId ID do usuario
     * @return DTO com resultado do reenvio
     * @throws EmailVerificationRateLimitException se limite de reenvios excedido
     */
    @Transactional
    public ResendVerificationResponseDTO resendVerification(UUID userId) {
        // 1. Buscar usuario
        User user = userRepository.findById(userId)
            .orElseThrow(() -> {
                log.warn("Resend verification attempted for non-existent user: {}", userId);
                // Retorna erro generico para nao revelar informacao
                return new RuntimeException("Usuario nao encontrado");
            });

        // 2. Verificar se ja esta verificado
        if (user.isEmailVerified()) {
            log.info("Resend verification attempted for already verified user: {}", userId);
            return ResendVerificationResponseDTO.alreadyVerified();
        }

        // 3. Verificar rate limit
        if (rateLimitService.isResendLimitReached(userId)) {
            long retryAfter = rateLimitService.getTimeUntilReset(userId);
            log.warn("Resend rate limit exceeded for user: {}", userId);
            throw new EmailVerificationRateLimitException(retryAfter > 0 ? retryAfter : 3600);
        }

        // 4. Registrar tentativa
        rateLimitService.recordResendAttempt(userId);

        // 5. Enviar novo email
        sendVerificationEmail(user);

        // 6. Retornar resposta
        int remaining = rateLimitService.getRemainingResends(userId);
        String maskedEmail = EmailMaskUtil.mask(user.getEmail());

        return ResendVerificationResponseDTO.success(maskedEmail, remaining);
    }

    /**
     * Confirma e-mail do usuario.
     *
     * @param token Token de verificacao em texto plano
     * @return DTO com resultado da confirmacao
     * @throws InvalidVerificationTokenException se token invalido/expirado/usado
     */
    @Transactional
    public EmailVerificationResponseDTO confirmEmail(String token) {
        // 1. Validar entrada
        if (token == null || token.isBlank()) {
            log.warn("Empty or null verification token attempted");
            throw InvalidVerificationTokenException.invalid();
        }

        // 2. Buscar token
        String tokenHash = TokenHashUtil.hashToken(token);
        EmailVerificationToken verifyToken = tokenRepository.findByTokenHash(tokenHash)
            .orElseThrow(() -> {
                log.warn("Invalid verification token attempted");
                return InvalidVerificationTokenException.invalid();
            });

        // 3. Verificar se ja foi usado
        if (verifyToken.isUsed()) {
            log.warn("Already used verification token attempted");
            throw InvalidVerificationTokenException.used();
        }

        // 4. Verificar expiracao
        if (verifyToken.isExpired()) {
            log.warn("Expired verification token attempted");
            throw InvalidVerificationTokenException.expired();
        }

        User user = verifyToken.getUser();

        // 5. Marcar token como usado
        verifyToken.markAsUsed();
        tokenRepository.save(verifyToken);

        // 6. Verificar e-mail do usuario
        user.setEmailVerified(true);
        user.setEmailVerifiedAt(LocalDateTime.now());
        userRepository.save(user);

        log.info("Email verified successfully for user: {}", user.getId());
        return EmailVerificationResponseDTO.success();
    }

    /**
     * Verifica se um usuario tem e-mail verificado.
     *
     * @param userId ID do usuario
     * @return true se verificado, false caso contrario
     */
    @Transactional(readOnly = true)
    public boolean isEmailVerified(UUID userId) {
        return userRepository.findById(userId)
            .map(User::isEmailVerified)
            .orElse(false);
    }
}
