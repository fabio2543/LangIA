package com.langia.backend.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import lombok.Getter;
import lombok.Setter;

/**
 * Configurações do módulo de recuperação de senha.
 * Valores podem ser sobrescritos em application.properties.
 */
@Configuration
@ConfigurationProperties(prefix = "password.reset")
@Getter
@Setter
public class PasswordResetProperties {

    private TokenConfig token = new TokenConfig();
    private HistoryConfig history = new HistoryConfig();
    private RateLimitConfig rateLimit = new RateLimitConfig();

    /**
     * Configurações do token de reset.
     */
    @Getter
    @Setter
    public static class TokenConfig {
        /**
         * Tempo de expiração do token em minutos.
         * Default: 30 minutos.
         */
        private int expirationMinutes = 30;
    }

    /**
     * Configurações do histórico de senhas.
     */
    @Getter
    @Setter
    public static class HistoryConfig {
        /**
         * Quantidade de senhas anteriores a verificar.
         * Default: 5 senhas.
         */
        private int count = 5;
    }

    /**
     * Configurações de rate limiting.
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
         * Janela de tempo para rate limiting em horas.
         * Default: 1 hora.
         */
        private int windowHours = 1;
    }
}
