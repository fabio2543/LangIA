package com.langia.backend.util;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.function.Function;

import javax.crypto.SecretKey;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import com.langia.backend.model.User;
import com.langia.backend.model.UserProfile;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.MalformedJwtException;
import io.jsonwebtoken.UnsupportedJwtException;
import io.jsonwebtoken.security.Keys;
import io.jsonwebtoken.security.SignatureException;
import lombok.extern.slf4j.Slf4j;

/**
 * Componente utilitário para gerenciamento de tokens JWT.
 * Responsável por gerar, validar e extrair informações de tokens de autenticação.
 */
@Component
@Slf4j
public class JwtUtil {

    @Value("${jwt.secret}")
    private String secret;

    @Value("${jwt.expiration}")
    private Long expiration;

    /**
     * Gera a chave secreta para assinatura dos tokens.
     *
     * @return chave secreta baseada no secret configurado
     */
    private SecretKey getSigningKey() {
        return Keys.hmacShaKeyFor(secret.getBytes());
    }

    /**
     * Gera um token JWT para um usuário autenticado.
     *
     * @param user usuário para o qual o token será gerado
     * @return token JWT assinado
     */
    public String generateToken(User user) {
        Map<String, Object> claims = new HashMap<>();
        claims.put("userId", user.getId().toString());
        claims.put("email", user.getEmail());
        claims.put("profile", user.getProfileCode().name());
        claims.put("name", user.getName());

        log.info("Gerando token JWT para usuário: {}", user.getEmail());

        return Jwts.builder()
                .claims(claims)
                .subject(user.getEmail())
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + expiration))
                .signWith(getSigningKey())
                .compact();
    }

    /**
     * Extrai todas as claims (informações) de um token JWT.
     *
     * @param token token JWT
     * @return claims contidas no token
     */
    private Claims extractAllClaims(String token) {
        return Jwts.parser()
                .verifyWith(getSigningKey())
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    /**
     * Extrai uma claim específica do token usando uma função de resolução.
     *
     * @param token token JWT
     * @param claimsResolver função para extrair a claim desejada
     * @return valor da claim extraída
     */
    private <T> T extractClaim(String token, Function<Claims, T> claimsResolver) {
        final Claims claims = extractAllClaims(token);
        return claimsResolver.apply(claims);
    }

    /**
     * Extrai o email (subject) do token.
     *
     * @param token token JWT
     * @return email do usuário
     */
    public String extractEmail(String token) {
        return extractClaim(token, Claims::getSubject);
    }

    /**
     * Extrai o ID do usuário do token.
     *
     * @param token token JWT
     * @return UUID do usuário
     */
    public UUID extractUserId(String token) {
        String userId = extractClaim(token, claims -> claims.get("userId", String.class));
        return UUID.fromString(userId);
    }

    /**
     * Extrai o perfil do usuário do token.
     *
     * @param token token JWT
     * @return perfil do usuário
     */
    public UserProfile extractUserProfile(String token) {
        String profile = extractClaim(token, claims -> claims.get("profile", String.class));
        return UserProfile.valueOf(profile);
    }

    /**
     * Extrai o nome do usuário do token.
     *
     * @param token token JWT
     * @return nome do usuário
     */
    public String extractUserName(String token) {
        return extractClaim(token, claims -> claims.get("name", String.class));
    }

    /**
     * Extrai a data de expiração do token.
     *
     * @param token token JWT
     * @return data de expiração
     */
    public Date extractExpiration(String token) {
        return extractClaim(token, Claims::getExpiration);
    }

    /**
     * Verifica se o token está expirado.
     *
     * @param token token JWT
     * @return true se o token estiver expirado, false caso contrário
     */
    public Boolean isTokenExpired(String token) {
        return extractExpiration(token).before(new Date());
    }

    /**
     * Valida um token JWT verificando assinatura e expiração.
     *
     * @param token token JWT a ser validado
     * @return true se o token for válido, false caso contrário
     */
    public Boolean validateToken(String token) {
        try {
            extractAllClaims(token);
            if (isTokenExpired(token)) {
                log.warn("Token expirado");
                return false;
            }
            log.debug("Token validado com sucesso");
            return true;
        } catch (SignatureException e) {
            log.error("Assinatura do token JWT inválida: {}", e.getMessage());
        } catch (MalformedJwtException e) {
            log.error("Token JWT malformado: {}", e.getMessage());
        } catch (ExpiredJwtException e) {
            log.error("Token JWT expirado: {}", e.getMessage());
        } catch (UnsupportedJwtException e) {
            log.error("Token JWT não suportado: {}", e.getMessage());
        } catch (IllegalArgumentException e) {
            log.error("Claims do JWT está vazia: {}", e.getMessage());
        }
        return false;
    }

    /**
     * Valida um token JWT e verifica se pertence ao email informado.
     *
     * @param token token JWT
     * @param email email para validação
     * @return true se o token for válido e pertencer ao email, false caso contrário
     */
    public Boolean validateToken(String token, String email) {
        final String tokenEmail = extractEmail(token);
        return (tokenEmail.equals(email) && !isTokenExpired(token));
    }
}
