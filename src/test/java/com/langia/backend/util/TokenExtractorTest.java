package com.langia.backend.util;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.langia.backend.config.AuthCookieProperties;
import com.langia.backend.exception.MissingTokenException;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;

/**
 * Testes para o extrator de tokens JWT.
 */
@ExtendWith(MockitoExtension.class)
class TokenExtractorTest {

    @Mock
    private AuthCookieProperties cookieProperties;

    @Mock
    private HttpServletRequest request;

    @InjectMocks
    private TokenExtractor tokenExtractor;

    private String validToken;

    @BeforeEach
    void setUp() {
        validToken = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.test";
    }

    // ========== Testes de extract() ==========

    @Test
    void deveExtrairTokenDoHeaderAuthorization() {
        // Act
        String result = tokenExtractor.extract("Bearer " + validToken);

        // Assert
        assertEquals(validToken, result);
    }

    @Test
    void deveLancarExcecaoQuandoHeaderNulo() {
        // Act & Assert
        assertThrows(MissingTokenException.class, () -> {
            tokenExtractor.extract(null);
        });
    }

    @Test
    void deveLancarExcecaoQuandoHeaderSemBearerPrefix() {
        // Act & Assert
        assertThrows(MissingTokenException.class, () -> {
            tokenExtractor.extract("InvalidPrefix " + validToken);
        });
    }

    @Test
    void deveLancarExcecaoQuandoTokenVazio() {
        // Act & Assert
        assertThrows(MissingTokenException.class, () -> {
            tokenExtractor.extract("Bearer ");
        });
    }

    @Test
    void deveLancarExcecaoQuandoTokenEmBranco() {
        // Act & Assert
        assertThrows(MissingTokenException.class, () -> {
            tokenExtractor.extract("Bearer    ");
        });
    }

    // ========== Testes de extractOrNull() ==========

    @Test
    void deveExtrairTokenOuRetornarNullComTokenValido() {
        // Act
        String result = tokenExtractor.extractOrNull("Bearer " + validToken);

        // Assert
        assertEquals(validToken, result);
    }

    @Test
    void deveRetornarNullQuandoHeaderNulo() {
        // Act
        String result = tokenExtractor.extractOrNull(null);

        // Assert
        assertNull(result);
    }

    @Test
    void deveRetornarNullQuandoHeaderSemBearerPrefix() {
        // Act
        String result = tokenExtractor.extractOrNull("InvalidPrefix " + validToken);

        // Assert
        assertNull(result);
    }

    @Test
    void deveRetornarNullQuandoTokenVazio() {
        // Act
        String result = tokenExtractor.extractOrNull("Bearer ");

        // Assert
        assertNull(result);
    }

    // ========== Testes de extractFromCookie() ==========

    @Test
    void deveExtrairTokenDoCookie() {
        // Arrange
        when(cookieProperties.getName()).thenReturn("auth_token");
        Cookie[] cookies = { new Cookie("auth_token", validToken) };
        when(request.getCookies()).thenReturn(cookies);

        // Act
        String result = tokenExtractor.extractFromCookie(request);

        // Assert
        assertEquals(validToken, result);
    }

    @Test
    void deveRetornarNullQuandoNaoHaCookies() {
        // Arrange
        when(request.getCookies()).thenReturn(null);

        // Act
        String result = tokenExtractor.extractFromCookie(request);

        // Assert
        assertNull(result);
    }

    @Test
    void deveRetornarNullQuandoCookieNaoEncontrado() {
        // Arrange
        when(cookieProperties.getName()).thenReturn("auth_token");
        Cookie[] cookies = { new Cookie("other_cookie", "value") };
        when(request.getCookies()).thenReturn(cookies);

        // Act
        String result = tokenExtractor.extractFromCookie(request);

        // Assert
        assertNull(result);
    }

    @Test
    void deveRetornarNullQuandoCookieTemValorVazio() {
        // Arrange
        when(cookieProperties.getName()).thenReturn("auth_token");
        Cookie[] cookies = { new Cookie("auth_token", "") };
        when(request.getCookies()).thenReturn(cookies);

        // Act
        String result = tokenExtractor.extractFromCookie(request);

        // Assert
        assertNull(result);
    }

    // ========== Testes de extractFromRequest() ==========

    @Test
    void devePriorizarCookieQuandoAmbosPresentes() {
        // Arrange
        when(cookieProperties.getName()).thenReturn("auth_token");
        Cookie[] cookies = { new Cookie("auth_token", "cookie_token") };
        when(request.getCookies()).thenReturn(cookies);

        // Act
        String result = tokenExtractor.extractFromRequest(request);

        // Assert
        assertEquals("cookie_token", result);
    }

    @Test
    void deveFazerFallbackParaHeaderQuandoNaoHaCookie() {
        // Arrange
        when(request.getCookies()).thenReturn(null);
        when(request.getHeader("Authorization")).thenReturn("Bearer " + validToken);

        // Act
        String result = tokenExtractor.extractFromRequest(request);

        // Assert
        assertEquals(validToken, result);
    }

    @Test
    void deveRetornarNullQuandoNenhumTokenEncontrado() {
        // Arrange
        when(request.getCookies()).thenReturn(null);
        when(request.getHeader("Authorization")).thenReturn(null);

        // Act
        String result = tokenExtractor.extractFromRequest(request);

        // Assert
        assertNull(result);
    }
}
