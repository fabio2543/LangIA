package com.langia.backend.service;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.Duration;
import java.util.HexFormat;
import java.util.Objects;

import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import com.langia.backend.config.JwtProperties;
import com.langia.backend.dto.UserSessionDTO;

import lombok.extern.slf4j.Slf4j;

/**
 * Handles persisted user sessions stored in Redis.
 */
@Service
@Slf4j
public class UserSessionService {

    private static final String SESSION_KEY_PREFIX = "session:";
    private static final String HASH_ALGORITHM = "SHA-256";

    private final RedisTemplate<String, UserSessionDTO> redisTemplate;
    private final Duration sessionTtl;

    public UserSessionService(RedisTemplate<String, UserSessionDTO> redisTemplate, JwtProperties jwtProperties) {
        this.redisTemplate = redisTemplate;
        this.sessionTtl = Duration.ofMillis(jwtProperties.getExpirationMs());
    }

    /**
     * Saves a session in Redis with a TTL that matches the JWT expiration.
     *
     * @param token       JWT token acting as the key
     * @param sessionData information stored for quick access
     */
    public void saveSession(String token, UserSessionDTO sessionData) {
        Objects.requireNonNull(sessionData, "Session data must not be null");
        String key = Objects.requireNonNull(buildKey(token), "Session key must not be null");
        Duration ttl = Objects.requireNonNull(sessionTtl, "Session TTL must not be null");
        redisTemplate.opsForValue().set(key, sessionData, ttl);
        log.debug("Session stored in Redis for user {} with TTL {} ms", sessionData.getUserId(),
                ttl.toMillis());
    }

    /**
     * Fetches a session associated with the provided token.
     *
     * @param token JWT token
     * @return stored session data or null when the session is missing/expired
     */
    public UserSessionDTO getSession(String token) {
        String key = Objects.requireNonNull(buildKey(token), "Session key must not be null");
        UserSessionDTO session = redisTemplate.opsForValue().get(key);
        if (session == null) {
            log.debug("Session not found or expired for token");
        }
        return session;
    }

    /**
     * Removes a session from Redis. Useful during logout or forceful invalidation.
     *
     * @param token JWT token
     */
    public void removeSession(String token) {
        String key = Objects.requireNonNull(buildKey(token), "Session key must not be null");
        Boolean deleted = redisTemplate.delete(key);
        if (Boolean.TRUE.equals(deleted)) {
            log.info("Session removed for token");
        } else {
            log.debug("No session to remove for token");
        }
    }

    private String buildKey(String token) {
        if (!StringUtils.hasText(token)) {
            throw new IllegalArgumentException("Token must not be null or empty");
        }
        return SESSION_KEY_PREFIX + hashToken(token);
    }

    private String hashToken(String token) {
        try {
            MessageDigest digest = MessageDigest.getInstance(HASH_ALGORITHM);
            byte[] hash = digest.digest(token.getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(hash);
        } catch (NoSuchAlgorithmException ex) {
            throw new IllegalStateException("Failed to hash token using " + HASH_ALGORITHM, ex);
        }
    }
}

