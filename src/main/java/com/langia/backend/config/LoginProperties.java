package com.langia.backend.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import lombok.Getter;
import lombok.Setter;

/**
 * Configurações do módulo de login.
 * Valores podem ser sobrescritos em application.properties.
 */
@Configuration
@ConfigurationProperties(prefix = "login")
@Getter
@Setter
public class LoginProperties {

    private RateLimitConfig rateLimit = new RateLimitConfig();

    /**
     * Configurações de rate limiting para login.
     */
    @Getter
    @Setter
    public static class RateLimitConfig {
        /**
         * Máximo de tentativas por IP.
         * Default: 10 tentativas.
         */
        private int maxAttemptsPerIp = 10;

        /**
         * Máximo de tentativas por email.
         * Default: 5 tentativas.
         */
        private int maxAttemptsPerEmail = 5;

        /**
         * Janela de tempo para rate limiting em minutos.
         * Default: 15 minutos.
         */
        private int windowMinutes = 15;

        /**
         * Tempo de bloqueio após exceder tentativas, em minutos.
         * Default: 30 minutos.
         */
        private int lockoutMinutes = 30;
    }
}
