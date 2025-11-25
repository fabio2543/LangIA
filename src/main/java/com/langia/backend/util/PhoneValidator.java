package com.langia.backend.util;

import java.util.Set;

import org.springframework.stereotype.Component;

import lombok.extern.slf4j.Slf4j;

/**
 * Validador de telefone brasileiro (fixo e celular).
 * Valida DDDs, formato de celular (9 dígitos) e fixo (8 dígitos).
 */
@Component
@Slf4j
public class PhoneValidator {

    private static final String COUNTRY_CODE = "55";
    private static final int MOBILE_LENGTH = 11; // DDD (2) + 9 dígitos
    private static final int LANDLINE_LENGTH = 10; // DDD (2) + 8 dígitos
    private static final char MOBILE_PREFIX = '9';

    // DDDs válidos no Brasil por região
    private static final Set<String> VALID_DDDS = Set.of(
            // Região Sudeste
            "11", "12", "13", "14", "15", "16", "17", "18", "19", // São Paulo
            "21", "22", "24", // Rio de Janeiro
            "27", "28", // Espírito Santo
            "31", "32", "33", "34", "35", "37", "38", // Minas Gerais

            // Região Sul
            "41", "42", "43", "44", "45", "46", // Paraná
            "47", "48", "49", // Santa Catarina
            "51", "53", "54", "55", // Rio Grande do Sul

            // Região Centro-Oeste
            "61", "62", "64", // Distrito Federal e Goiás
            "65", "66", // Mato Grosso
            "67", // Mato Grosso do Sul

            // Região Nordeste
            "71", "73", "74", "75", "77", // Bahia
            "79", // Sergipe
            "81", "87", // Pernambuco
            "82", // Alagoas
            "83", // Paraíba
            "84", // Rio Grande do Norte
            "85", "88", // Ceará
            "86", "89", // Piauí
            "98", "99", // Maranhão

            // Região Norte
            "91", "93", "94", // Pará
            "92", "97", // Amazonas
            "68", // Acre
            "69", // Rondônia
            "95", // Roraima
            "96", // Amapá
            "63"  // Tocantins
    );

    /**
     * Valida um telefone brasileiro (fixo ou celular).
     *
     * @param phone telefone a ser validado (pode conter formatação)
     * @return true se o telefone for válido, false caso contrário
     */
    public boolean isValid(String phone) {
        if (phone == null || phone.isBlank()) {
            log.debug("Telefone inválido: vazio ou nulo");
            return false;
        }

        // Remove formatação
        String cleanPhone = cleanPhone(phone);

        // Verifica se contém apenas dígitos
        if (!cleanPhone.matches("\\d+")) {
            log.debug("Telefone inválido: contém caracteres não numéricos");
            return false;
        }

        // Valida comprimento e formato
        if (cleanPhone.length() == MOBILE_LENGTH) {
            return validateMobile(cleanPhone);
        } else if (cleanPhone.length() == LANDLINE_LENGTH) {
            return validateLandline(cleanPhone);
        } else {
            log.debug("Telefone inválido: comprimento incorreto ({})", cleanPhone.length());
            return false;
        }
    }

    /**
     * Remove formatação do telefone.
     * Remove +55, parênteses, espaços, hífen.
     *
     * @param phone telefone com ou sem formatação
     * @return telefone limpo contendo apenas dígitos
     */
    public String cleanPhone(String phone) {
        if (phone == null) {
            return "";
        }

        // Remove código do país, formatação e espaços
        return phone.replaceAll("\\+55|[()\\-\\s]", "");
    }

    /**
     * Valida telefone celular (11 dígitos: DDD + 9XXXX-XXXX).
     *
     * @param phone telefone limpo (apenas dígitos)
     * @return true se for celular válido
     */
    private boolean validateMobile(String phone) {
        // Extrai DDD
        String ddd = phone.substring(0, 2);

        // Valida DDD
        if (!VALID_DDDS.contains(ddd)) {
            log.debug("Telefone inválido: DDD não existe ({})", ddd);
            return false;
        }

        // Celular deve começar com 9 após o DDD
        if (phone.charAt(2) != MOBILE_PREFIX) {
            log.debug("Celular inválido: não começa com 9 após o DDD");
            return false;
        }

        return true;
    }

    /**
     * Valida telefone fixo (10 dígitos: DDD + XXXX-XXXX).
     *
     * @param phone telefone limpo (apenas dígitos)
     * @return true se for fixo válido
     */
    private boolean validateLandline(String phone) {
        // Extrai DDD
        String ddd = phone.substring(0, 2);

        // Valida DDD
        if (!VALID_DDDS.contains(ddd)) {
            log.debug("Telefone inválido: DDD não existe ({})", ddd);
            return false;
        }

        // Fixo NÃO deve começar com 9 após o DDD
        if (phone.charAt(2) == MOBILE_PREFIX) {
            log.debug("Fixo inválido: começa com 9 após o DDD");
            return false;
        }

        return true;
    }

    /**
     * Verifica se o telefone é celular.
     *
     * @param phone telefone (pode conter formatação)
     * @return true se for celular válido
     */
    public boolean isMobile(String phone) {
        if (!isValid(phone)) {
            return false;
        }

        String cleanPhone = cleanPhone(phone);
        return cleanPhone.length() == MOBILE_LENGTH;
    }

    /**
     * Verifica se o telefone é fixo.
     *
     * @param phone telefone (pode conter formatação)
     * @return true se for fixo válido
     */
    public boolean isLandline(String phone) {
        if (!isValid(phone)) {
            return false;
        }

        String cleanPhone = cleanPhone(phone);
        return cleanPhone.length() == LANDLINE_LENGTH;
    }

    /**
     * Formata telefone para padrão internacional WhatsApp (+5511987654321).
     *
     * @param phone telefone (pode conter formatação)
     * @return telefone formatado para WhatsApp ou string vazia se inválido
     */
    public String formatForWhatsApp(String phone) {
        if (!isValid(phone)) {
            return "";
        }

        String cleanPhone = cleanPhone(phone);
        return "+" + COUNTRY_CODE + cleanPhone;
    }

    /**
     * Formata telefone para exibição brasileira.
     * Celular: (11) 98765-4321
     * Fixo: (11) 3456-7890
     *
     * @param phone telefone (pode conter formatação)
     * @return telefone formatado ou string vazia se inválido
     */
    public String formatForDisplay(String phone) {
        if (!isValid(phone)) {
            return "";
        }

        String cleanPhone = cleanPhone(phone);

        if (cleanPhone.length() == MOBILE_LENGTH) {
            // (XX) 9XXXX-XXXX
            return String.format("(%s) %s-%s",
                    cleanPhone.substring(0, 2),
                    cleanPhone.substring(2, 7),
                    cleanPhone.substring(7, 11)
            );
        } else {
            // (XX) XXXX-XXXX
            return String.format("(%s) %s-%s",
                    cleanPhone.substring(0, 2),
                    cleanPhone.substring(2, 6),
                    cleanPhone.substring(6, 10)
            );
        }
    }

    /**
     * Extrai o DDD do telefone.
     *
     * @param phone telefone (pode conter formatação)
     * @return DDD (2 dígitos) ou string vazia se inválido
     */
    public String extractDDD(String phone) {
        if (phone == null || phone.isBlank()) {
            return "";
        }

        String cleanPhone = cleanPhone(phone);
        if (cleanPhone.length() >= 10) {
            return cleanPhone.substring(0, 2);
        }

        return "";
    }

    /**
     * Verifica se o DDD existe no Brasil.
     *
     * @param ddd código de área (2 dígitos)
     * @return true se o DDD for válido
     */
    public boolean isValidDDD(String ddd) {
        return VALID_DDDS.contains(ddd);
    }
}
