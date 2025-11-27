package com.langia.backend.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import lombok.Data;

/**
 * Propriedades de configuração para o cookie de autenticação.
 * Valores configuráveis via variáveis de ambiente no .env
 */
@Component
@ConfigurationProperties(prefix = "auth.cookie")
@Data
public class AuthCookieProperties {

    /**
     * Nome do cookie de autenticação.
     */
    private String name;

    /**
     * Se o cookie deve ser enviado apenas em conexões HTTPS.
     */
    private boolean secure;

    /**
     * Política SameSite para proteção contra CSRF.
     */
    private String sameSite;
}
