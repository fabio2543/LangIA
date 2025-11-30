package com.langia.backend.util;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import org.junit.jupiter.api.Test;

/**
 * Testes para o utilitário de mascaramento de email.
 */
class EmailMaskUtilTest {

    @Test
    void deveMascararEmailSimples() {
        // Act
        String result = EmailMaskUtil.mask("usuario@email.com");

        // Assert
        assertNotNull(result);
        assertEquals("u***@e***.com", result);
    }

    @Test
    void deveMascararEmailComSubdominio() {
        // Act
        String result = EmailMaskUtil.mask("usuario@mail.example.com");

        // Assert
        assertNotNull(result);
        assertEquals("u***@m***.example.com", result);
    }

    @Test
    void deveMascararEmailComMultiplosSubdominios() {
        // Act
        String result = EmailMaskUtil.mask("usuario@sub.domain.example.org");

        // Assert
        assertNotNull(result);
        assertEquals("u***@s***.domain.example.org", result);
    }

    @Test
    void deveRetornarMascaraPadraoParaEmailNulo() {
        // Act
        String result = EmailMaskUtil.mask(null);

        // Assert
        assertEquals("***@***.***", result);
    }

    @Test
    void deveRetornarMascaraPadraoParaEmailSemArroba() {
        // Act
        String result = EmailMaskUtil.mask("emailinvalido");

        // Assert
        assertEquals("***@***.***", result);
    }

    @Test
    void deveRetornarMascaraPadraoParaEmailVazio() {
        // Act
        String result = EmailMaskUtil.mask("");

        // Assert
        assertEquals("***@***.***", result);
    }

    @Test
    void deveMascararEmailComUmCaracterLocal() {
        // Act
        String result = EmailMaskUtil.mask("a@example.com");

        // Assert
        assertNotNull(result);
        assertEquals("a***@e***.com", result);
    }

    @Test
    void deveMascararEmailComDominioSimples() {
        // Act
        String result = EmailMaskUtil.mask("usuario@a.br");

        // Assert
        assertNotNull(result);
        assertEquals("u***@a***.br", result);
    }
}
