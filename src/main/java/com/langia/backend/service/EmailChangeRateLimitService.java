package com.langia.backend.service;

import java.time.Duration;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * Service para rate limiting de verificação de troca de e-mail.
 * Previne brute-force do código de 6 dígitos limitando tentativas por usuário.
 * Usa Redis para contagem com TTL automático.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class EmailChangeRateLimitService {

    private final RedisTemplate<String, String> redisTemplate;

    @Value("${email.change.rate-limit.max-attempts:5}")
    private int maxAttemptsPerWindow;

    @Value("${email.change.rate-limit.window-minutes:15}")
    private int windowMinutes;

    @Value("${email.change.rate-limit.lockout-minutes:60}")
    private int lockoutMinutes;

    private static final String ATTEMPT_KEY_PREFIX = "email_change:verify:";
    private static final String LOCKOUT_KEY_PREFIX = "email_change:lockout:";

    /**
     * Verifica se o usuário está bloqueado por excesso de tentativas.
     *
     * @param userId ID do usuário
     * @return true se bloqueado, false se pode tentar
     */
    public boolean isLocked(UUID userId) {
        String lockoutKey = LOCKOUT_KEY_PREFIX + userId.toString();
        return Boolean.TRUE.equals(redisTemplate.hasKey(lockoutKey));
    }

    /**
     * Verifica se o limite de tentativas foi atingido.
     *
     * @param userId ID do usuário
     * @return true se limite atingido (deve bloquear), false se pode prosseguir
     */
    public boolean isAttemptLimitReached(UUID userId) {
        if (isLocked(userId)) {
            return true;
        }

        String key = ATTEMPT_KEY_PREFIX + userId.toString();
        String count = redisTemplate.opsForValue().get(key);

        if (count == null) {
            return false;
        }

        int attempts = Integer.parseInt(count);
        return attempts >= maxAttemptsPerWindow;
    }

    /**
     * Registra uma tentativa de verificação falha.
     * Após atingir o limite, aplica lockout.
     *
     * @param userId ID do usuário
     * @return true se usuário foi bloqueado após esta tentativa
     */
    public boolean recordFailedAttempt(UUID userId) {
        String key = ATTEMPT_KEY_PREFIX + userId.toString();
        Long count = redisTemplate.opsForValue().increment(key);

        // Define TTL apenas na primeira tentativa
        if (count != null && count == 1) {
            Duration window = Duration.ofMinutes(windowMinutes);
            redisTemplate.expire(key, window);
            log.debug("Started email change verification rate limit window for user: {}", userId);
        }

        log.warn("Failed email change verification attempt {} for user: {}", count, userId);

        // Se atingiu o limite, aplica lockout
        if (count != null && count >= maxAttemptsPerWindow) {
            applyLockout(userId);
            return true;
        }

        return false;
    }

    /**
     * Aplica lockout ao usuário.
     */
    private void applyLockout(UUID userId) {
        String lockoutKey = LOCKOUT_KEY_PREFIX + userId.toString();
        redisTemplate.opsForValue().set(lockoutKey, "locked", Duration.ofMinutes(lockoutMinutes));
        log.warn("User {} locked out from email change verification for {} minutes", userId, lockoutMinutes);
    }

    /**
     * Limpa as tentativas após verificação bem-sucedida.
     *
     * @param userId ID do usuário
     */
    public void clearAttempts(UUID userId) {
        String key = ATTEMPT_KEY_PREFIX + userId.toString();
        redisTemplate.delete(key);
        log.debug("Cleared email change verification attempts for user: {}", userId);
    }

    /**
     * Retorna quantas tentativas restam para o usuário.
     *
     * @param userId ID do usuário
     * @return Número de tentativas restantes
     */
    public int getRemainingAttempts(UUID userId) {
        if (isLocked(userId)) {
            return 0;
        }

        String key = ATTEMPT_KEY_PREFIX + userId.toString();
        String count = redisTemplate.opsForValue().get(key);

        int used = count != null ? Integer.parseInt(count) : 0;
        return Math.max(0, maxAttemptsPerWindow - used);
    }

    /**
     * Retorna o tempo restante do lockout em segundos.
     *
     * @param userId ID do usuário
     * @return Segundos até desbloquear, ou -1 se não está bloqueado
     */
    public long getLockoutTimeRemaining(UUID userId) {
        String lockoutKey = LOCKOUT_KEY_PREFIX + userId.toString();
        Long ttl = redisTemplate.getExpire(lockoutKey);
        return ttl != null && ttl > 0 ? ttl : -1;
    }

    /**
     * Reseta o lockout e tentativas para um usuário (uso administrativo).
     *
     * @param userId ID do usuário
     */
    public void resetLockout(UUID userId) {
        String attemptKey = ATTEMPT_KEY_PREFIX + userId.toString();
        String lockoutKey = LOCKOUT_KEY_PREFIX + userId.toString();
        redisTemplate.delete(attemptKey);
        redisTemplate.delete(lockoutKey);
        log.info("Reset email change rate limit and lockout for user: {}", userId);
    }
}
