package com.langia.backend.util;

import org.springframework.stereotype.Component;

import com.langia.backend.exception.MissingTokenException;

/**
 * Componente utilitário para extração e validação de tokens JWT do header Authorization.
 */
@Component
public class TokenExtractor {

    private static final String BEARER_PREFIX = "Bearer ";

    /**
     * Extrai o token JWT do header Authorization.
     * Lança exceção se o header estiver ausente ou mal formatado.
     *
     * @param authorizationHeader conteúdo do header Authorization
     * @return token JWT extraído
     * @throws MissingTokenException se header ausente ou formato inválido
     */
    public String extract(String authorizationHeader) {
        if (authorizationHeader == null || !authorizationHeader.startsWith(BEARER_PREFIX)) {
            throw new MissingTokenException();
        }

        String token = authorizationHeader.substring(BEARER_PREFIX.length());
        if (token.isBlank()) {
            throw new MissingTokenException();
        }

        return token;
    }

    /**
     * Extrai o token JWT do header Authorization sem lançar exceção.
     *
     * @param authorizationHeader conteúdo do header Authorization
     * @return token JWT extraído ou null se formato inválido
     */
    public String extractOrNull(String authorizationHeader) {
        if (authorizationHeader == null || !authorizationHeader.startsWith(BEARER_PREFIX)) {
            return null;
        }

        String token = authorizationHeader.substring(BEARER_PREFIX.length());
        return token.isBlank() ? null : token;
    }
}
