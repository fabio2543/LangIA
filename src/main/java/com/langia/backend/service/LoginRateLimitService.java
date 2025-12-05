package com.langia.backend.service;

import java.time.Duration;

import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import com.langia.backend.config.LoginProperties;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * Service para rate limiting de tentativas de login.
 * Usa Redis para contagem com TTL automático.
 * Protege contra ataques de força bruta e enumeração de credenciais.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class LoginRateLimitService {

    private final RedisTemplate<String, String> redisTemplate;
    private final LoginProperties properties;

    private static final String IP_KEY_PREFIX = "login_limit:ip:";
    private static final String EMAIL_KEY_PREFIX = "login_limit:email:";

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
            log.warn("Login bloqueado para IP {} - excedeu {} tentativas", ipAddress, attempts);
        }

        return blocked;
    }

    /**
     * Verifica se o email está bloqueado por excesso de tentativas.
     *
     * @param email Email normalizado
     * @return true se bloqueado, false se pode prosseguir
     */
    public boolean isEmailBlocked(String email) {
        String key = EMAIL_KEY_PREFIX + normalizeEmail(email);
        String count = redisTemplate.opsForValue().get(key);

        if (count == null) {
            return false;
        }

        int attempts = Integer.parseInt(count);
        boolean blocked = attempts >= properties.getRateLimit().getMaxAttemptsPerEmail();

        if (blocked) {
            log.warn("Login bloqueado para email {} - excedeu {} tentativas", email, attempts);
        }

        return blocked;
    }

    /**
     * Registra uma tentativa de login falha.
     *
     * @param ipAddress Endereço IP do cliente
     * @param email Email tentado
     */
    public void recordFailedAttempt(String ipAddress, String email) {
        recordIpAttempt(ipAddress);
        recordEmailAttempt(email);
    }

    /**
     * Registra uma tentativa de login por IP.
     */
    private void recordIpAttempt(String ipAddress) {
        String key = IP_KEY_PREFIX + ipAddress;
        Long count = redisTemplate.opsForValue().increment(key);

        if (count != null && count == 1) {
            Duration window = Duration.ofMinutes(properties.getRateLimit().getWindowMinutes());
            redisTemplate.expire(key, window);
            log.debug("Iniciada janela de rate limit para IP {}", ipAddress);
        }

        log.debug("Tentativa {} de login para IP {}", count, ipAddress);
    }

    /**
     * Registra uma tentativa de login por email.
     */
    private void recordEmailAttempt(String email) {
        String key = EMAIL_KEY_PREFIX + normalizeEmail(email);
        Long count = redisTemplate.opsForValue().increment(key);

        if (count != null && count == 1) {
            Duration window = Duration.ofMinutes(properties.getRateLimit().getWindowMinutes());
            redisTemplate.expire(key, window);
            log.debug("Iniciada janela de rate limit para email {}", email);
        }

        log.debug("Tentativa {} de login para email {}", count, email);
    }

    /**
     * Reseta os contadores após login bem-sucedido.
     *
     * @param ipAddress Endereço IP do cliente
     * @param email Email do usuário
     */
    public void resetOnSuccess(String ipAddress, String email) {
        String ipKey = IP_KEY_PREFIX + ipAddress;
        String emailKey = EMAIL_KEY_PREFIX + normalizeEmail(email);

        redisTemplate.delete(ipKey);
        redisTemplate.delete(emailKey);

        log.debug("Rate limit resetado para IP {} e email {}", ipAddress, email);
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
     * Retorna quantas tentativas restam para o IP.
     *
     * @param ipAddress Endereço IP do cliente
     * @return Número de tentativas restantes
     */
    public int getRemainingAttemptsForIp(String ipAddress) {
        String key = IP_KEY_PREFIX + ipAddress;
        String count = redisTemplate.opsForValue().get(key);

        int used = count != null ? Integer.parseInt(count) : 0;
        int max = properties.getRateLimit().getMaxAttemptsPerIp();

        return Math.max(0, max - used);
    }

    /**
     * Retorna quantas tentativas restam para o email.
     *
     * @param email Email do cliente
     * @return Número de tentativas restantes
     */
    public int getRemainingAttemptsForEmail(String email) {
        String key = EMAIL_KEY_PREFIX + normalizeEmail(email);
        String count = redisTemplate.opsForValue().get(key);

        int used = count != null ? Integer.parseInt(count) : 0;
        int max = properties.getRateLimit().getMaxAttemptsPerEmail();

        return Math.max(0, max - used);
    }

    /**
     * Reseta manualmente os contadores (uso administrativo).
     *
     * @param ipAddress Endereço IP
     */
    public void resetIpAttempts(String ipAddress) {
        String key = IP_KEY_PREFIX + ipAddress;
        redisTemplate.delete(key);
        log.info("Rate limit de login resetado para IP {}", ipAddress);
    }

    /**
     * Reseta manualmente os contadores de email (uso administrativo).
     *
     * @param email Email do cliente
     */
    public void resetEmailAttempts(String email) {
        String key = EMAIL_KEY_PREFIX + normalizeEmail(email);
        redisTemplate.delete(key);
        log.info("Rate limit de login resetado para email {}", email);
    }

    /**
     * Normaliza email para lowercase.
     */
    private String normalizeEmail(String email) {
        return email != null ? email.toLowerCase().trim() : "";
    }
}
