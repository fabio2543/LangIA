package com.langia.backend.util;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import jakarta.servlet.http.HttpServletRequest;

/**
 * Testes para o utilitário de extração de endereço IP.
 */
@ExtendWith(MockitoExtension.class)
class IpAddressUtilTest {

    @Mock
    private HttpServletRequest request;

    @Test
    void deveExtrairIpDoHeaderXForwardedFor() {
        // Arrange
        when(request.getHeader("X-Forwarded-For")).thenReturn("192.168.1.100");

        // Act
        String result = IpAddressUtil.getClientIp(request);

        // Assert
        assertEquals("192.168.1.100", result);
    }

    @Test
    void deveExtrairPrimeiroIpDoXForwardedForComMultiplosIps() {
        // Arrange
        when(request.getHeader("X-Forwarded-For")).thenReturn("192.168.1.100, 10.0.0.1, 172.16.0.1");

        // Act
        String result = IpAddressUtil.getClientIp(request);

        // Assert
        assertEquals("192.168.1.100", result);
    }

    @Test
    void deveExtrairIpDoHeaderXRealIP() {
        // Arrange
        when(request.getHeader("X-Forwarded-For")).thenReturn(null);
        when(request.getHeader("X-Real-IP")).thenReturn("10.0.0.50");

        // Act
        String result = IpAddressUtil.getClientIp(request);

        // Assert
        assertEquals("10.0.0.50", result);
    }

    @Test
    void deveExtrairIpDoHeaderProxyClientIP() {
        // Arrange
        when(request.getHeader("X-Forwarded-For")).thenReturn(null);
        when(request.getHeader("X-Real-IP")).thenReturn(null);
        when(request.getHeader("Proxy-Client-IP")).thenReturn("172.16.0.100");

        // Act
        String result = IpAddressUtil.getClientIp(request);

        // Assert
        assertEquals("172.16.0.100", result);
    }

    @Test
    void deveExtrairIpDoHeaderWLProxyClientIP() {
        // Arrange
        when(request.getHeader("X-Forwarded-For")).thenReturn(null);
        when(request.getHeader("X-Real-IP")).thenReturn(null);
        when(request.getHeader("Proxy-Client-IP")).thenReturn(null);
        when(request.getHeader("WL-Proxy-Client-IP")).thenReturn("192.168.0.200");

        // Act
        String result = IpAddressUtil.getClientIp(request);

        // Assert
        assertEquals("192.168.0.200", result);
    }

    @Test
    void deveFazerFallbackParaRemoteAddrQuandoNenhumHeaderPresente() {
        // Arrange
        when(request.getHeader("X-Forwarded-For")).thenReturn(null);
        when(request.getHeader("X-Real-IP")).thenReturn(null);
        when(request.getHeader("Proxy-Client-IP")).thenReturn(null);
        when(request.getHeader("WL-Proxy-Client-IP")).thenReturn(null);
        when(request.getHeader("HTTP_X_FORWARDED_FOR")).thenReturn(null);
        when(request.getHeader("HTTP_CLIENT_IP")).thenReturn(null);
        when(request.getRemoteAddr()).thenReturn("127.0.0.1");

        // Act
        String result = IpAddressUtil.getClientIp(request);

        // Assert
        assertEquals("127.0.0.1", result);
    }

    @Test
    void deveIgnorarHeaderComValorUnknown() {
        // Arrange
        when(request.getHeader("X-Forwarded-For")).thenReturn("unknown");
        when(request.getHeader("X-Real-IP")).thenReturn("192.168.1.50");

        // Act
        String result = IpAddressUtil.getClientIp(request);

        // Assert
        assertEquals("192.168.1.50", result);
    }

    @Test
    void deveIgnorarHeaderComValorVazio() {
        // Arrange
        when(request.getHeader("X-Forwarded-For")).thenReturn("");
        when(request.getHeader("X-Real-IP")).thenReturn("10.0.0.25");

        // Act
        String result = IpAddressUtil.getClientIp(request);

        // Assert
        assertEquals("10.0.0.25", result);
    }

    @Test
    void deveTrimmarEspacosDoIp() {
        // Arrange
        when(request.getHeader("X-Forwarded-For")).thenReturn("  192.168.1.100  ");

        // Act
        String result = IpAddressUtil.getClientIp(request);

        // Assert
        assertEquals("192.168.1.100", result);
    }

    @Test
    void deveTrimmarEspacosDoIpEmListaDeIps() {
        // Arrange
        when(request.getHeader("X-Forwarded-For")).thenReturn("  192.168.1.100  ,  10.0.0.1  ");

        // Act
        String result = IpAddressUtil.getClientIp(request);

        // Assert
        assertEquals("192.168.1.100", result);
    }
}
