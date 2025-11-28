package com.langia.backend.util;

import jakarta.servlet.http.HttpServletRequest;
import lombok.experimental.UtilityClass;

/**
 * Utilitário para extração de endereço IP do cliente.
 * Considera headers de proxy para obter o IP real.
 */
@UtilityClass
public class IpAddressUtil {

    private static final String[] IP_HEADERS = {
        "X-Forwarded-For",
        "X-Real-IP",
        "Proxy-Client-IP",
        "WL-Proxy-Client-IP",
        "HTTP_X_FORWARDED_FOR",
        "HTTP_CLIENT_IP"
    };

    /**
     * Extrai o endereço IP real do cliente considerando proxies.
     *
     * @param request HttpServletRequest
     * @return Endereço IP do cliente
     */
    public static String getClientIp(HttpServletRequest request) {
        for (String header : IP_HEADERS) {
            String ip = request.getHeader(header);
            if (isValidIp(ip)) {
                // X-Forwarded-For pode conter múltiplos IPs separados por vírgula
                // O primeiro é o IP original do cliente
                return ip.split(",")[0].trim();
            }
        }
        return request.getRemoteAddr();
    }

    /**
     * Verifica se o valor do header é um IP válido (não nulo, não vazio, não "unknown").
     */
    private static boolean isValidIp(String ip) {
        return ip != null && !ip.isEmpty() && !"unknown".equalsIgnoreCase(ip);
    }
}
