package com.langia.backend.service.validator;

import java.util.Set;

import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

/**
 * Utility component responsible for validating and normalizing user-provided
 * identifiers.
 */
@Component
public class UserDataValidator {

    private static final int CPF_LENGTH = 11;
    private static final Set<String> VALID_DDDS = Set.of(
            // Sudeste
            "11", "12", "13", "14", "15", "16", "17", "18", "19",
            "21", "22", "24",
            "27", "28",
            "31", "32", "33", "34", "35", "37", "38",
            // Sul
            "41", "42", "43", "44", "45", "46",
            "47", "48", "49",
            "51", "53", "54", "55",
            // Centro-Oeste
            "61", "62", "64",
            "65", "66",
            "67",
            // Nordeste
            "71", "73", "74", "75", "77",
            "79",
            "81", "87",
            "82",
            "83",
            "84",
            "85", "88",
            "86", "89",
            "98", "99",
            // Norte
            "91", "93", "94",
            "92", "97",
            "68",
            "69",
            "95",
            "96",
            "63");
    private static final String COUNTRY_CODE = "55";
    private static final String INTERNATIONAL_PREFIX = "+55";

    /**
     * Normalizes a CPF by stripping non-digit characters and validates its check
     * digits.
     *
     * @param cpf input CPF (may contain punctuation)
     * @return normalized CPF containing only digits
     */
    public String normalizeAndValidateCpf(String cpf) {
        if (!StringUtils.hasText(cpf)) {
            throw new IllegalArgumentException("CPF is required.");
        }

        String digits = cpf.replaceAll("\\D", "");

        if (digits.length() != CPF_LENGTH) {
            throw new IllegalArgumentException("CPF must contain exactly 11 digits.");
        }

        if (digits.chars().distinct().count() == 1) {
            throw new IllegalArgumentException("CPF cannot contain repeated digits.");
        }

        if (!hasValidCpfDigits(digits)) {
            throw new IllegalArgumentException("CPF check digits are invalid.");
        }

        return digits;
    }

    /**
     * Normalizes a phone number by stripping formatting, validating DDD and number
     * rules,
     * and storing it as +55XXXXXXXXXXX for WhatsApp-friendly integrations.
     *
     * @param phone input phone number
     * @return normalized phone containing country code and digits only
     */
    public String normalizeAndValidatePhone(String phone) {
        if (!StringUtils.hasText(phone)) {
            throw new IllegalArgumentException("Phone is required.");
        }

        String digits = phone.replaceAll("\\D", "");
        if (digits.startsWith(COUNTRY_CODE) && digits.length() > CPF_LENGTH) {
            digits = digits.substring(COUNTRY_CODE.length());
        }

        if (digits.length() != 10 && digits.length() != 11) {
            throw new IllegalArgumentException("Phone number must contain 10 or 11 digits (including DDD).");
        }

        String ddd = digits.substring(0, 2);
        if (!VALID_DDDS.contains(ddd)) {
            throw new IllegalArgumentException("Invalid DDD for Brazilian phone number.");
        }

        String numberPart = digits.substring(2);

        if (numberPart.length() == 9) {
            if (numberPart.charAt(0) != '9') {
                throw new IllegalArgumentException("Mobile numbers must start with 9 after the DDD.");
            }
        } else if (numberPart.length() == 8) {
            if (numberPart.charAt(0) == '9') {
                throw new IllegalArgumentException("Landline numbers cannot start with 9 after the DDD.");
            }
        } else {
            throw new IllegalArgumentException("Invalid phone number length.");
        }

        return INTERNATIONAL_PREFIX + ddd + numberPart;
    }

    private boolean hasValidCpfDigits(String digits) {
        int firstDigit = calculateCpfDigit(digits, 9);
        int secondDigit = calculateCpfDigit(digits, 10);
        return digits.charAt(9) == Character.forDigit(firstDigit, 10)
                && digits.charAt(10) == Character.forDigit(secondDigit, 10);
    }

    private int calculateCpfDigit(String digits, int length) {
        int sum = 0;
        int weight = length + 1;
        for (int i = 0; i < length; i++) {
            int num = Character.digit(digits.charAt(i), 10);
            sum += num * (weight - i);
        }
        int remainder = sum % 11;
        return remainder < 2 ? 0 : 11 - remainder;
    }
}
