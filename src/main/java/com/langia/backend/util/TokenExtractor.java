package com.langia.backend.util;

import org.springframework.stereotype.Component;

import com.langia.backend.config.AuthCookieProperties;
import com.langia.backend.exception.MissingTokenException;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;

/**
 * Componente utilitário para extração e validação de tokens JWT.
 * Suporta extração tanto do header Authorization quanto de cookies HttpOnly.
 */
@Component
@RequiredArgsConstructor
public class TokenExtractor {

    private static final String BEARER_PREFIX = "Bearer ";

    private final AuthCookieProperties cookieProperties;

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

    /**
     * Extrai o token JWT do cookie HttpOnly.
     *
     * @param request requisição HTTP
     * @return token JWT extraído ou null se cookie não encontrado
     */
    public String extractFromCookie(HttpServletRequest request) {
        if (request.getCookies() == null) {
            return null;
        }

        String cookieName = cookieProperties.getName();
        for (Cookie cookie : request.getCookies()) {
            if (cookieName.equals(cookie.getName())) {
                String token = cookie.getValue();
                return (token != null && !token.isBlank()) ? token : null;
            }
        }

        return null;
    }

    /**
     * Extrai o token JWT priorizando cookie HttpOnly, com fallback para header Authorization.
     * Esta é a forma mais segura de extração pois cookies HttpOnly não são acessíveis via JavaScript.
     *
     * @param request requisição HTTP
     * @return token JWT extraído ou null se não encontrado
     */
    public String extractFromRequest(HttpServletRequest request) {
        // Prioriza cookie HttpOnly (mais seguro contra XSS)
        String tokenFromCookie = extractFromCookie(request);
        if (tokenFromCookie != null) {
            return tokenFromCookie;
        }

        // Fallback para header Authorization (compatibilidade)
        return extractOrNull(request.getHeader("Authorization"));
    }
}
