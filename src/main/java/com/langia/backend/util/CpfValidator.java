package com.langia.backend.util;

import java.util.Set;

import org.springframework.stereotype.Component;

import lombok.extern.slf4j.Slf4j;

/**
 * Validador de CPF brasileiro.
 * Implementa o algoritmo oficial de validação de CPF com verificação de dígitos.
 */
@Component
@Slf4j
public class CpfValidator {

    private static final int CPF_LENGTH = 11;
    private static final Set<String> INVALID_CPFS = Set.of(
            "00000000000",
            "11111111111",
            "22222222222",
            "33333333333",
            "44444444444",
            "55555555555",
            "66666666666",
            "77777777777",
            "88888888888",
            "99999999999"
    );

    /**
     * Valida um CPF brasileiro seguindo as regras oficiais.
     *
     * @param cpf CPF a ser validado (pode conter formatação)
     * @return true se o CPF for válido, false caso contrário
     */
    public boolean isValid(String cpf) {
        if (cpf == null || cpf.isBlank()) {
            log.debug("CPF inválido: vazio ou nulo");
            return false;
        }

        // Remove formatação (pontos e hífen)
        String cleanCpf = cleanCpf(cpf);

        // Verifica se tem 11 dígitos
        if (cleanCpf.length() != CPF_LENGTH) {
            log.debug("CPF inválido: não possui 11 dígitos");
            return false;
        }

        // Verifica se é apenas números
        if (!cleanCpf.matches("\\d{11}")) {
            log.debug("CPF inválido: contém caracteres não numéricos");
            return false;
        }

        // Verifica se não é sequência repetida
        if (INVALID_CPFS.contains(cleanCpf)) {
            log.debug("CPF inválido: sequência repetida");
            return false;
        }

        // Valida dígitos verificadores
        return validateDigits(cleanCpf);
    }

    /**
     * Remove formatação do CPF (pontos, hífen, espaços).
     *
     * @param cpf CPF com ou sem formatação
     * @return CPF limpo contendo apenas dígitos
     */
    public String cleanCpf(String cpf) {
        if (cpf == null) {
            return "";
        }
        return cpf.replaceAll("[.\\-\\s]", "");
    }

    /**
     * Valida os dígitos verificadores do CPF.
     *
     * @param cpf CPF limpo (apenas números)
     * @return true se os dígitos verificadores estiverem corretos
     */
    private boolean validateDigits(String cpf) {
        try {
            // Calcula primeiro dígito verificador
            int firstDigit = calculateDigit(cpf.substring(0, 9), 10);
            if (firstDigit != Character.getNumericValue(cpf.charAt(9))) {
                log.debug("CPF inválido: primeiro dígito verificador incorreto");
                return false;
            }

            // Calcula segundo dígito verificador
            int secondDigit = calculateDigit(cpf.substring(0, 10), 11);
            if (secondDigit != Character.getNumericValue(cpf.charAt(10))) {
                log.debug("CPF inválido: segundo dígito verificador incorreto");
                return false;
            }

            return true;
        } catch (Exception e) {
            log.error("Erro ao validar dígitos do CPF", e);
            return false;
        }
    }

    /**
     * Calcula um dígito verificador do CPF.
     *
     * @param cpfPartial parte do CPF para cálculo (9 ou 10 dígitos)
     * @param startWeight peso inicial (10 para primeiro dígito, 11 para segundo)
     * @return dígito verificador calculado
     */
    private int calculateDigit(String cpfPartial, int startWeight) {
        int sum = 0;
        int weight = startWeight;

        for (int i = 0; i < cpfPartial.length(); i++) {
            int digit = Character.getNumericValue(cpfPartial.charAt(i));
            sum += digit * weight;
            weight--;
        }

        int remainder = sum % 11;
        return remainder < 2 ? 0 : 11 - remainder;
    }

    /**
     * Formata um CPF limpo no padrão XXX.XXX.XXX-XX.
     *
     * @param cpf CPF limpo (apenas números)
     * @return CPF formatado ou string vazia se inválido
     */
    public String formatCpf(String cpf) {
        if (cpf == null || cpf.length() != CPF_LENGTH) {
            return "";
        }

        String cleanCpf = cleanCpf(cpf);
        if (cleanCpf.length() != CPF_LENGTH) {
            return "";
        }

        return String.format("%s.%s.%s-%s",
                cleanCpf.substring(0, 3),
                cleanCpf.substring(3, 6),
                cleanCpf.substring(6, 9),
                cleanCpf.substring(9, 11)
        );
    }
}
