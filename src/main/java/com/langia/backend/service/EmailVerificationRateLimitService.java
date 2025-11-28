package com.langia.backend.service;

import java.time.Duration;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * Service para rate limiting de reenvios de verificacao de e-mail.
 * Usa Redis para contagem com TTL automatico.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class EmailVerificationRateLimitService {

    private final RedisTemplate<String, String> redisTemplate;

    @Value("${email.verification.rate-limit.max-resends-per-hour:3}")
    private int maxResendsPerHour;

    @Value("${email.verification.rate-limit.window-hours:1}")
    private int windowHours;

    private static final String RESEND_KEY_PREFIX = "email_verify:resend:";

    /**
     * Verifica se o limite de reenvios foi atingido para um usuario.
     *
     * @param userId ID do usuario
     * @return true se limite atingido, false se pode prosseguir
     */
    public boolean isResendLimitReached(UUID userId) {
        String key = RESEND_KEY_PREFIX + userId.toString();
        String count = redisTemplate.opsForValue().get(key);

        if (count == null) {
            return false;
        }

        int attempts = Integer.parseInt(count);
        boolean limited = attempts >= maxResendsPerHour;

        if (limited) {
            log.warn("Email verification resend limit reached for user: {}", userId);
        }

        return limited;
    }

    /**
     * Registra uma tentativa de reenvio de verificacao.
     *
     * @param userId ID do usuario
     */
    public void recordResendAttempt(UUID userId) {
        String key = RESEND_KEY_PREFIX + userId.toString();
        Long count = redisTemplate.opsForValue().increment(key);

        // Define TTL apenas na primeira tentativa
        if (count != null && count == 1) {
            Duration window = Duration.ofHours(windowHours);
            redisTemplate.expire(key, window);
            log.debug("Started resend rate limit window for user: {}", userId);
        }

        log.debug("Recorded resend attempt {} for user: {}", count, userId);
    }

    /**
     * Retorna quantos reenvios restam para o usuario.
     *
     * @param userId ID do usuario
     * @return Numero de reenvios restantes
     */
    public int getRemainingResends(UUID userId) {
        String key = RESEND_KEY_PREFIX + userId.toString();
        String count = redisTemplate.opsForValue().get(key);

        int used = count != null ? Integer.parseInt(count) : 0;
        return Math.max(0, maxResendsPerHour - used);
    }

    /**
     * Retorna o tempo restante ate o reset do rate limit em segundos.
     *
     * @param userId ID do usuario
     * @return Segundos ate expirar, ou -1 se nao ha limite ativo
     */
    public long getTimeUntilReset(UUID userId) {
        String key = RESEND_KEY_PREFIX + userId.toString();
        Long ttl = redisTemplate.getExpire(key);
        return ttl != null ? ttl : -1;
    }

    /**
     * Reseta o contador de reenvios para um usuario (uso administrativo).
     *
     * @param userId ID do usuario
     */
    public void resetResendAttempts(UUID userId) {
        String key = RESEND_KEY_PREFIX + userId.toString();
        redisTemplate.delete(key);
        log.info("Reset resend rate limit for user: {}", userId);
    }
}
