package com.langia.backend.dto;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Set;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;

/**
 * Testes para validação do LoginRequestDTO.
 * Valida as constraints de email e senha.
 */
class LoginRequestDTOTest {

    private Validator validator;

    @BeforeEach
    void setUp() {
        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        validator = factory.getValidator();
    }

    @Test
    void deveValidarLoginRequestValido() {
        // Given
        LoginRequestDTO dto = new LoginRequestDTO("user@example.com", "password123");

        // When
        Set<ConstraintViolation<LoginRequestDTO>> violations = validator.validate(dto);

        // Then
        assertTrue(violations.isEmpty(), "Não deve haver violações para dados válidos");
    }

    @Test
    void deveRejeitarEmailVazio() {
        // Given
        LoginRequestDTO dto = new LoginRequestDTO("", "password123");

        // When
        Set<ConstraintViolation<LoginRequestDTO>> violations = validator.validate(dto);

        // Then
        assertFalse(violations.isEmpty(), "Deve haver violação para email vazio");
        assertTrue(violations.stream()
                .anyMatch(v -> v.getMessage().equals("Email is required")),
                "Deve conter mensagem de email obrigatório");
    }

    @Test
    void deveRejeitarEmailNulo() {
        // Given
        LoginRequestDTO dto = new LoginRequestDTO(null, "password123");

        // When
        Set<ConstraintViolation<LoginRequestDTO>> violations = validator.validate(dto);

        // Then
        assertFalse(violations.isEmpty(), "Deve haver violação para email nulo");
    }

    @Test
    void deveRejeitarEmailInvalido() {
        // Given
        LoginRequestDTO dto = new LoginRequestDTO("emailinvalido", "password123");

        // When
        Set<ConstraintViolation<LoginRequestDTO>> violations = validator.validate(dto);

        // Then
        assertFalse(violations.isEmpty(), "Deve haver violação para email inválido");
        assertTrue(violations.stream()
                .anyMatch(v -> v.getMessage().equals("Email must be valid")),
                "Deve conter mensagem de email inválido");
    }

    @Test
    void deveRejeitarSenhaVazia() {
        // Given
        LoginRequestDTO dto = new LoginRequestDTO("user@example.com", "");

        // When
        Set<ConstraintViolation<LoginRequestDTO>> violations = validator.validate(dto);

        // Then
        assertFalse(violations.isEmpty(), "Deve haver violação para senha vazia");
        assertTrue(violations.stream()
                .anyMatch(v -> v.getMessage().equals("Password is required")),
                "Deve conter mensagem de senha obrigatória");
    }

    @Test
    void deveRejeitarSenhaNula() {
        // Given
        LoginRequestDTO dto = new LoginRequestDTO("user@example.com", null);

        // When
        Set<ConstraintViolation<LoginRequestDTO>> violations = validator.validate(dto);

        // Then
        assertFalse(violations.isEmpty(), "Deve haver violação para senha nula");
    }

    @Test
    void deveAceitarSenhaDeQualquerTamanho() {
        // Given - senha curta
        LoginRequestDTO dto = new LoginRequestDTO("user@example.com", "12");

        // When
        Set<ConstraintViolation<LoginRequestDTO>> violations = validator.validate(dto);

        // Then
        assertTrue(violations.isEmpty(), "Não deve validar tamanho mínimo de senha no DTO");
    }

    @Test
    void deveValidarMultiplosFormatosDeEmail() {
        // Given
        String[] emailsValidos = {
                "user@example.com",
                "user.name@example.com",
                "user+tag@example.co.uk",
                "user_name@example-domain.com"
        };

        // When & Then
        for (String email : emailsValidos) {
            LoginRequestDTO dto = new LoginRequestDTO(email, "password");
            Set<ConstraintViolation<LoginRequestDTO>> violations = validator.validate(dto);
            assertTrue(violations.isEmpty(),
                    "Email " + email + " deveria ser considerado válido");
        }
    }

    @Test
    void deveTestarGettersESetters() {
        // Given
        LoginRequestDTO dto = new LoginRequestDTO();

        // When
        dto.setEmail("test@example.com");
        dto.setPassword("testpass");

        // Then
        assertEquals("test@example.com", dto.getEmail());
        assertEquals("testpass", dto.getPassword());
    }
}
