package com.langia.backend.util;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.Base64;

import lombok.experimental.UtilityClass;

/**
 * Utilitário para geração e hash de tokens de recuperação de senha.
 * Usa SHA-256 para hash (performático para tokens aleatórios).
 */
@UtilityClass
public class TokenHashUtil {

    private static final SecureRandom SECURE_RANDOM = new SecureRandom();
    private static final int TOKEN_BYTE_LENGTH = 32;

    /**
     * Gera um token seguro de 32 bytes codificado em Base64 URL-safe.
     *
     * @return Token aleatório de 43 caracteres
     */
    public static String generateSecureToken() {
        byte[] bytes = new byte[TOKEN_BYTE_LENGTH];
        SECURE_RANDOM.nextBytes(bytes);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
    }

    /**
     * Gera o hash SHA-256 de um token.
     *
     * @param token Token em texto plano
     * @return Hash SHA-256 em hexadecimal (64 caracteres)
     */
    public static String hashToken(String token) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(token.getBytes(StandardCharsets.UTF_8));
            return bytesToHex(hash);
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("SHA-256 algorithm not available", e);
        }
    }

    /**
     * Verifica se um token corresponde a um hash.
     *
     * @param token Token em texto plano
     * @param hash  Hash SHA-256 esperado
     * @return true se o token corresponde ao hash
     */
    public static boolean verifyToken(String token, String hash) {
        String computedHash = hashToken(token);
        return computedHash.equalsIgnoreCase(hash);
    }

    private static String bytesToHex(byte[] bytes) {
        StringBuilder sb = new StringBuilder(bytes.length * 2);
        for (byte b : bytes) {
            sb.append(String.format("%02x", b));
        }
        return sb.toString();
    }
}
