package com.langia.backend.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.util.StringUtils;

import jakarta.annotation.PostConstruct;
import lombok.Getter;
import lombok.Setter;

@Configuration
@ConfigurationProperties(prefix = "jwt")
@Getter
@Setter
public class JwtProperties {

    private static final int MIN_SECRET_LENGTH = 32;
    private static final String DEPRECATED_DEFAULT = "${JWT_SECRET_KEY}";

    /**
     * Secret key used to sign JWT tokens. Must be provided via configuration.
     */
    private String secretKey;

    /**
     * Token expiration time in milliseconds.
     * Default: 1 hour (3600000 ms)
     */
    private long expirationMs = 3600000L; // 1 hour

    @PostConstruct
    void validateSecretKey() {
        if (!StringUtils.hasText(secretKey)) {
            throw new IllegalStateException(
                    "Property 'jwt.secret-key' must be provided and contain at least " + MIN_SECRET_LENGTH
                            + " characters.");
        }

        if (DEPRECATED_DEFAULT.equals(secretKey)) {
            throw new IllegalStateException(
                    "Property 'jwt.secret-key' is using a deprecated insecure value. Provide a new random secret.");
        }

        if (secretKey.length() < MIN_SECRET_LENGTH) {
            throw new IllegalStateException(
                    "Property 'jwt.secret-key' must contain at least " + MIN_SECRET_LENGTH + " characters.");
        }
    }
}

