package com.langia.backend.service;

import java.time.Duration;

import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import com.langia.backend.config.PasswordResetProperties;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * Service para rate limiting de tentativas de recuperação de senha.
 * Usa Redis para contagem com TTL automático.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class PasswordResetRateLimitService {

    private final RedisTemplate<String, String> redisTemplate;
    private final PasswordResetProperties properties;

    private static final String IP_KEY_PREFIX = "reset_limit:ip:";
    private static final String EMAIL_KEY_PREFIX = "reset_limit:email:";

    /**
     * Verifica se o IP está bloqueado por excesso de tentativas.
     *
     * @param ipAddress Endereço IP do cliente
     * @return true se bloqueado, false se pode prosseguir
     */
    public boolean isIpBlocked(String ipAddress) {
        String key = IP_KEY_PREFIX + ipAddress;
        String count = redisTemplate.opsForValue().get(key);

        if (count == null) {
            return false;
        }

        int attempts = Integer.parseInt(count);
        boolean blocked = attempts >= properties.getRateLimit().getMaxAttemptsPerIp();

        if (blocked) {
            log.warn("IP {} blocked - exceeded {} attempts", ipAddress, attempts);
        }

        return blocked;
    }

    /**
     * Registra uma tentativa de recuperação de senha.
     *
     * @param ipAddress Endereço IP do cliente
     */
    public void recordAttempt(String ipAddress) {
        String key = IP_KEY_PREFIX + ipAddress;
        Long count = redisTemplate.opsForValue().increment(key);

        // Define TTL apenas na primeira tentativa
        if (count != null && count == 1) {
            Duration window = Duration.ofHours(properties.getRateLimit().getWindowHours());
            redisTemplate.expire(key, window);
            log.debug("Started rate limit window for IP {}", ipAddress);
        }

        log.debug("Recorded attempt {} for IP {}", count, ipAddress);
    }

    /**
     * Retorna quantas tentativas restam para o IP.
     *
     * @param ipAddress Endereço IP do cliente
     * @return Número de tentativas restantes
     */
    public int getRemainingAttempts(String ipAddress) {
        String key = IP_KEY_PREFIX + ipAddress;
        String count = redisTemplate.opsForValue().get(key);

        int used = count != null ? Integer.parseInt(count) : 0;
        int max = properties.getRateLimit().getMaxAttemptsPerIp();

        return Math.max(0, max - used);
    }

    /**
     * Retorna o tempo restante até o reset do rate limit em segundos.
     *
     * @param ipAddress Endereço IP do cliente
     * @return Segundos até expirar, ou -1 se não há limite ativo
     */
    public long getTimeUntilReset(String ipAddress) {
        String key = IP_KEY_PREFIX + ipAddress;
        Long ttl = redisTemplate.getExpire(key);
        return ttl != null ? ttl : -1;
    }

    /**
     * Reseta o contador de tentativas para um IP (uso administrativo).
     *
     * @param ipAddress Endereço IP do cliente
     */
    public void resetAttempts(String ipAddress) {
        String key = IP_KEY_PREFIX + ipAddress;
        redisTemplate.delete(key);
        log.info("Reset rate limit for IP {}", ipAddress);
    }

    // ============================================
    // Rate Limiting por Email
    // ============================================

    /**
     * Verifica se o email atingiu o limite de tentativas.
     * Nota: Não bloqueia, apenas indica se deve ignorar silenciosamente.
     *
     * @param email Email normalizado
     * @return true se limite atingido, false se pode prosseguir
     */
    public boolean isEmailLimitReached(String email) {
        String key = EMAIL_KEY_PREFIX + email;
        String count = redisTemplate.opsForValue().get(key);

        if (count == null) {
            return false;
        }

        int attempts = Integer.parseInt(count);
        boolean limited = attempts >= properties.getRateLimit().getMaxAttemptsPerEmail();

        if (limited) {
            log.info("Email {} rate limited - {} attempts", email, attempts);
        }

        return limited;
    }

    /**
     * Registra uma tentativa de recuperação de senha por email.
     *
     * @param email Email normalizado
     */
    public void recordEmailAttempt(String email) {
        String key = EMAIL_KEY_PREFIX + email;
        Long count = redisTemplate.opsForValue().increment(key);

        // Define TTL apenas na primeira tentativa
        if (count != null && count == 1) {
            Duration window = Duration.ofHours(properties.getRateLimit().getWindowHours());
            redisTemplate.expire(key, window);
            log.debug("Started rate limit window for email {}", email);
        }

        log.debug("Recorded attempt {} for email {}", count, email);
    }

    /**
     * Registra tentativas tanto por IP quanto por email.
     *
     * @param ipAddress Endereço IP do cliente
     * @param email Email normalizado
     */
    public void recordAttempt(String ipAddress, String email) {
        recordAttempt(ipAddress);
        recordEmailAttempt(email);
    }

    /**
     * Reseta o contador de tentativas para um email (uso administrativo).
     *
     * @param email Email do cliente
     */
    public void resetEmailAttempts(String email) {
        String key = EMAIL_KEY_PREFIX + email;
        redisTemplate.delete(key);
        log.info("Reset rate limit for email {}", email);
    }
}
