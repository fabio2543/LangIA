package com.langia.backend.service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.langia.backend.config.PasswordResetProperties;
import com.langia.backend.dto.ValidateTokenResponseDTO;
import com.langia.backend.exception.InvalidResetTokenException;
import com.langia.backend.exception.PasswordRecentlyUsedException;
import com.langia.backend.exception.RateLimitExceededException;
import com.langia.backend.model.PasswordHistory;
import com.langia.backend.model.PasswordResetToken;
import com.langia.backend.model.User;
import com.langia.backend.repository.PasswordHistoryRepository;
import com.langia.backend.repository.PasswordResetTokenRepository;
import com.langia.backend.repository.UserRepository;
import com.langia.backend.util.EmailMaskUtil;
import com.langia.backend.util.TokenHashUtil;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * Serviço de negócio para recuperação de senha.
 * Implementa a lógica de solicitação, validação e redefinição de senha.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class PasswordResetService {

    private final UserRepository userRepository;
    private final PasswordResetTokenRepository tokenRepository;
    private final PasswordHistoryRepository historyRepository;
    private final PasswordResetRateLimitService rateLimitService;
    private final SessionService sessionService;
    private final EmailService emailService;
    private final BCryptPasswordEncoder passwordEncoder;
    private final PasswordResetProperties properties;

    @Value("${app.frontend.url:http://localhost:5173}")
    private String frontendUrl;

    /**
     * Solicita recuperação de senha para um email.
     *
     * @param email     Email do usuário (será normalizado)
     * @param ipAddress Endereço IP do cliente
     * @return true sempre (não revela se email existe)
     * @throws RateLimitExceededException se IP exceder limite de tentativas
     */
    @Transactional
    public boolean requestPasswordReset(String email, String ipAddress) {
        // 1. Normalizar email
        String normalizedEmail = normalizeEmail(email);
        log.info("Password reset requested for email: {}", normalizedEmail);

        // 2. Verificar rate limiting por email (silencioso)
        if (rateLimitService.isEmailLimitReached(normalizedEmail)) {
            log.warn("Email rate limit reached for: {}", normalizedEmail);
            // Retorna sucesso para não revelar informação
            return true;
        }

        // 3. Verificar rate limiting por IP (lança exceção)
        if (rateLimitService.isIpBlocked(ipAddress)) {
            long retryAfter = rateLimitService.getTimeUntilReset(ipAddress);
            log.warn("IP rate limit exceeded: {}", ipAddress);
            throw new RateLimitExceededException(retryAfter > 0 ? retryAfter : 3600);
        }

        // 4. Registrar tentativa
        rateLimitService.recordAttempt(ipAddress, normalizedEmail);

        // 5. Buscar usuário (silenciosamente)
        Optional<User> userOptional = userRepository.findByEmail(normalizedEmail);

        if (userOptional.isEmpty()) {
            log.info("Password reset attempted for non-existent email: {}", normalizedEmail);
            // Retorna sucesso para não revelar que email não existe
            return true;
        }

        User user = userOptional.get();

        // 6. Invalidar tokens anteriores
        int invalidated = tokenRepository.invalidateAllUserTokens(user.getId(), LocalDateTime.now());
        if (invalidated > 0) {
            log.debug("Invalidated {} previous tokens for user: {}", invalidated, user.getId());
        }

        // 7. Gerar novo token
        String plainToken = TokenHashUtil.generateSecureToken();
        String tokenHash = TokenHashUtil.hashToken(plainToken);
        LocalDateTime expiresAt = LocalDateTime.now()
                .plusMinutes(properties.getToken().getExpirationMinutes());

        PasswordResetToken resetToken = PasswordResetToken.builder()
                .user(user)
                .tokenHash(tokenHash)
                .expiresAt(expiresAt)
                .build();

        tokenRepository.save(resetToken);
        log.info("Password reset token generated for user: {}", user.getId());

        // 8. Enviar email
        String resetLink = frontendUrl + "/reset-password?token=" + plainToken;
        String expirationTime = properties.getToken().getExpirationMinutes() + " minutos";

        emailService.sendPasswordResetEmail(
                user.getEmail(),
                user.getName(),
                resetLink,
                expirationTime
        );

        return true;
    }

    /**
     * Valida um token de recuperação de senha.
     *
     * @param token Token em texto plano
     * @return DTO com resultado da validação
     */
    @Transactional(readOnly = true)
    public ValidateTokenResponseDTO validateToken(String token) {
        String tokenHash = TokenHashUtil.hashToken(token);

        Optional<PasswordResetToken> tokenOptional = tokenRepository.findByTokenHash(tokenHash);

        if (tokenOptional.isEmpty()) {
            log.warn("Invalid token attempted");
            return ValidateTokenResponseDTO.invalid();
        }

        PasswordResetToken resetToken = tokenOptional.get();

        if (resetToken.isUsed()) {
            log.warn("Already used token attempted");
            return ValidateTokenResponseDTO.builder()
                    .valid(false)
                    .error("TOKEN_ALREADY_USED")
                    .message("Este link já foi utilizado. Solicite uma nova recuperação de senha.")
                    .build();
        }

        if (resetToken.isExpired()) {
            log.warn("Expired token attempted");
            return ValidateTokenResponseDTO.builder()
                    .valid(false)
                    .error("TOKEN_EXPIRED")
                    .message("Este link expirou. Solicite uma nova recuperação de senha.")
                    .build();
        }

        User user = resetToken.getUser();
        String maskedEmail = EmailMaskUtil.mask(user.getEmail());

        log.info("Token validated successfully for user: {}", user.getId());
        return ValidateTokenResponseDTO.valid(maskedEmail);
    }

    /**
     * Redefine a senha do usuário.
     *
     * @param token       Token em texto plano
     * @param newPassword Nova senha
     * @return true se senha foi alterada com sucesso
     * @throws InvalidResetTokenException    se token inválido
     * @throws PasswordRecentlyUsedException se senha foi usada recentemente
     */
    @Transactional
    public boolean resetPassword(String token, String newPassword) {
        // 1. Revalidar token
        String tokenHash = TokenHashUtil.hashToken(token);
        PasswordResetToken resetToken = tokenRepository.findByTokenHash(tokenHash)
                .orElseThrow(() -> {
                    log.warn("Reset password attempted with invalid token");
                    return new InvalidResetTokenException();
                });

        if (!resetToken.isValid()) {
            log.warn("Reset password attempted with expired/used token");
            throw new InvalidResetTokenException();
        }

        User user = resetToken.getUser();

        // 2. Validar complexidade da senha
        List<String> errors = validatePasswordComplexity(newPassword);
        if (!errors.isEmpty()) {
            log.warn("Password complexity validation failed: {}", errors);
            throw new IllegalArgumentException(String.join(", ", errors));
        }

        // 3. Verificar histórico de senhas
        int historyCount = properties.getHistory().getCount();
        List<PasswordHistory> recentPasswords = historyRepository.findLastPasswords(user.getId(), historyCount);

        // Verificar senha atual do usuário
        if (passwordEncoder.matches(newPassword, user.getPassword())) {
            log.warn("User {} attempted to reuse current password", user.getId());
            throw new PasswordRecentlyUsedException();
        }

        // Verificar senhas do histórico
        for (PasswordHistory history : recentPasswords) {
            if (passwordEncoder.matches(newPassword, history.getPasswordHash())) {
                log.warn("User {} attempted to reuse recent password", user.getId());
                throw new PasswordRecentlyUsedException();
            }
        }

        // 4. Salvar senha atual no histórico
        PasswordHistory passwordHistory = PasswordHistory.builder()
                .user(user)
                .passwordHash(user.getPassword())
                .build();
        historyRepository.save(passwordHistory);

        // Limpar histórico antigo (manter apenas as últimas N)
        historyRepository.deleteOldestKeeping(user.getId(), historyCount);

        // 5. Atualizar senha
        String newPasswordHash = passwordEncoder.encode(newPassword);
        user.setPassword(newPasswordHash);
        userRepository.save(user);

        // 6. Marcar token como usado
        resetToken.setUsedAt(LocalDateTime.now());
        tokenRepository.save(resetToken);

        // 7. Invalidar todas as sessões do usuário
        long sessionsRemoved = sessionService.removeAllUserSessions(user.getId().toString());
        log.info("Removed {} sessions for user: {}", sessionsRemoved, user.getId());

        // 8. Enviar email de confirmação
        emailService.sendPasswordChangedEmail(user.getEmail(), user.getName());

        log.info("Password reset successfully for user: {}", user.getId());
        return true;
    }

    /**
     * Valida a complexidade da senha.
     *
     * @param password Senha a validar
     * @return Lista de erros (vazia se válida)
     */
    public List<String> validatePasswordComplexity(String password) {
        List<String> errors = new ArrayList<>();

        if (password == null || password.length() < 8) {
            errors.add("Senha deve ter no mínimo 8 caracteres");
        }

        if (password != null) {
            if (!password.matches(".*[A-Z].*")) {
                errors.add("Senha deve conter pelo menos 1 letra maiúscula");
            }

            if (!password.matches(".*[a-z].*")) {
                errors.add("Senha deve conter pelo menos 1 letra minúscula");
            }

            if (!password.matches(".*\\d.*")) {
                errors.add("Senha deve conter pelo menos 1 número");
            }

            if (!password.matches(".*[!@#$%^&*()_+\\-=\\[\\]{};':\"\\\\|,.<>/?].*")) {
                errors.add("Senha deve conter pelo menos 1 caractere especial (!@#$%^&*...)");
            }
        }

        return errors;
    }

    /**
     * Normaliza o email (trim + lowercase).
     */
    private String normalizeEmail(String email) {
        return email != null ? email.trim().toLowerCase() : null;
    }
}
