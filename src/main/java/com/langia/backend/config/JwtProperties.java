package com.langia.backend.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import lombok.Getter;
import lombok.Setter;

@Configuration
@ConfigurationProperties(prefix = "jwt")
@Getter
@Setter
public class JwtProperties {

    /**
     * Secret key used to sign JWT tokens.
     * Should be set via environment variable JWT_SECRET_KEY.
     * Default value is for development only and should be changed in production.
     */
    private String secretKey = "default-secret-key-change-in-production-min-256-bits";

    /**
     * Token expiration time in milliseconds.
     * Default: 1 hour (3600000 ms)
     */
    private long expirationMs = 3600000L; // 1 hour
}

