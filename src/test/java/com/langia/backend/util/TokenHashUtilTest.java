package com.langia.backend.util;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

/**
 * Testes para o utilitário de geração e hash de tokens.
 */
class TokenHashUtilTest {

    // ========== Testes de generateSecureToken() ==========

    @Test
    void deveGerarTokenNaoNulo() {
        // Act
        String token = TokenHashUtil.generateSecureToken();

        // Assert
        assertNotNull(token);
    }

    @Test
    void deveGerarTokenNaoVazio() {
        // Act
        String token = TokenHashUtil.generateSecureToken();

        // Assert
        assertFalse(token.isEmpty());
    }

    @Test
    void deveGerarTokenCom43Caracteres() {
        // Act
        String token = TokenHashUtil.generateSecureToken();

        // Assert
        assertEquals(43, token.length());
    }

    @Test
    void deveGerarTokensUnicos() {
        // Act
        String token1 = TokenHashUtil.generateSecureToken();
        String token2 = TokenHashUtil.generateSecureToken();

        // Assert
        assertNotEquals(token1, token2);
    }

    @Test
    void deveGerarTokenUrlSafe() {
        // Act
        String token = TokenHashUtil.generateSecureToken();

        // Assert - Não deve conter caracteres não URL-safe
        assertFalse(token.contains("+"));
        assertFalse(token.contains("/"));
        assertFalse(token.contains("="));
    }

    // ========== Testes de hashToken() ==========

    @Test
    void deveGerarHashNaoNulo() {
        // Act
        String hash = TokenHashUtil.hashToken("test-token");

        // Assert
        assertNotNull(hash);
    }

    @Test
    void deveGerarHashCom64Caracteres() {
        // Act
        String hash = TokenHashUtil.hashToken("test-token");

        // Assert - SHA-256 produz 64 caracteres hexadecimais
        assertEquals(64, hash.length());
    }

    @Test
    void deveGerarMesmoHashParaMesmoToken() {
        // Act
        String hash1 = TokenHashUtil.hashToken("test-token");
        String hash2 = TokenHashUtil.hashToken("test-token");

        // Assert
        assertEquals(hash1, hash2);
    }

    @Test
    void deveGerarHashesDiferentesParaTokensDiferentes() {
        // Act
        String hash1 = TokenHashUtil.hashToken("token-1");
        String hash2 = TokenHashUtil.hashToken("token-2");

        // Assert
        assertNotEquals(hash1, hash2);
    }

    @Test
    void deveGerarHashHexadecimal() {
        // Act
        String hash = TokenHashUtil.hashToken("test-token");

        // Assert - Deve conter apenas caracteres hexadecimais
        assertTrue(hash.matches("^[0-9a-f]+$"));
    }

    // ========== Testes de verifyToken() ==========

    @Test
    void deveVerificarTokenCorretamente() {
        // Arrange
        String token = "my-secret-token";
        String hash = TokenHashUtil.hashToken(token);

        // Act
        boolean result = TokenHashUtil.verifyToken(token, hash);

        // Assert
        assertTrue(result);
    }

    @Test
    void deveRetornarFalseParaTokenIncorreto() {
        // Arrange
        String correctToken = "correct-token";
        String wrongToken = "wrong-token";
        String hash = TokenHashUtil.hashToken(correctToken);

        // Act
        boolean result = TokenHashUtil.verifyToken(wrongToken, hash);

        // Assert
        assertFalse(result);
    }

    @Test
    void deveVerificarTokenComHashMaiusculo() {
        // Arrange
        String token = "my-token";
        String hash = TokenHashUtil.hashToken(token).toUpperCase();

        // Act
        boolean result = TokenHashUtil.verifyToken(token, hash);

        // Assert
        assertTrue(result);
    }

    @Test
    void deveVerificarTokenGeradoAleatoriamente() {
        // Arrange
        String token = TokenHashUtil.generateSecureToken();
        String hash = TokenHashUtil.hashToken(token);

        // Act
        boolean result = TokenHashUtil.verifyToken(token, hash);

        // Assert
        assertTrue(result);
    }
}
