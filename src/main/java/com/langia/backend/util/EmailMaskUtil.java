package com.langia.backend.util;

import lombok.experimental.UtilityClass;

/**
 * Utilitário para mascarar emails para exibição segura.
 */
@UtilityClass
public class EmailMaskUtil {

    /**
     * Mascara um email para exibição.
     * Exemplo: "usuario@email.com" -> "u***@e***.com"
     *
     * @param email Email a ser mascarado
     * @return Email mascarado
     */
    public static String mask(String email) {
        if (email == null || !email.contains("@")) {
            return "***@***.***";
        }

        String[] parts = email.split("@");
        String local = parts[0];
        String domain = parts[1];

        String maskedLocal = local.charAt(0) + "***";

        String[] domainParts = domain.split("\\.");
        String maskedDomain = domainParts[0].charAt(0) + "***";

        StringBuilder sb = new StringBuilder(maskedLocal + "@" + maskedDomain);
        for (int i = 1; i < domainParts.length; i++) {
            sb.append(".").append(domainParts[i]);
        }

        return sb.toString();
    }
}
