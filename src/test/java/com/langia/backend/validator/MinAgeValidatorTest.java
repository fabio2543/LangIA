package com.langia.backend.validator;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.LocalDate;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import com.langia.backend.annotation.MinAge;

import jakarta.validation.ConstraintValidatorContext;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Testes para o validador de idade mínima.
 * Implementa verificação do AC-DP-004: Validação de idade mínima.
 */
class MinAgeValidatorTest {

    private MinAgeValidator validator;
    private ConstraintValidatorContext context;

    @BeforeEach
    void setUp() {
        validator = new MinAgeValidator();
        context = mock(ConstraintValidatorContext.class);

        // Configura a anotação mock para idade mínima de 13 anos
        MinAge minAge = mock(MinAge.class);
        when(minAge.value()).thenReturn(13);
        validator.initialize(minAge);
    }

    // ========== AC-DP-004: Validação de idade mínima ==========

    @Test
    @DisplayName("AC-DP-004: Deve aceitar idade exatamente igual à mínima (13 anos)")
    void deveAceitarIdadeExatamenteIgualMinima() {
        LocalDate birthDate = LocalDate.now().minusYears(13);
        assertTrue(validator.isValid(birthDate, context));
    }

    @Test
    @DisplayName("AC-DP-004: Deve aceitar idade maior que a mínima (18 anos)")
    void deveAceitarIdadeMaiorQueMinima() {
        LocalDate birthDate = LocalDate.now().minusYears(18);
        assertTrue(validator.isValid(birthDate, context));
    }

    @Test
    @DisplayName("AC-DP-004: Deve aceitar idade muito maior que a mínima (50 anos)")
    void deveAceitarIdadeMuitoMaiorQueMinima() {
        LocalDate birthDate = LocalDate.now().minusYears(50);
        assertTrue(validator.isValid(birthDate, context));
    }

    @Test
    @DisplayName("AC-DP-004: Deve rejeitar idade menor que a mínima (12 anos)")
    void deveRejeitarIdadeMenorQueMinima() {
        LocalDate birthDate = LocalDate.now().minusYears(12);
        assertFalse(validator.isValid(birthDate, context));
    }

    @Test
    @DisplayName("AC-DP-004: Deve rejeitar idade muito menor que a mínima (5 anos)")
    void deveRejeitarIdadeMuitoMenorQueMinima() {
        LocalDate birthDate = LocalDate.now().minusYears(5);
        assertFalse(validator.isValid(birthDate, context));
    }

    @Test
    @DisplayName("AC-DP-004: Deve rejeitar data de nascimento no futuro")
    void deveRejeitarDataNoFuturo() {
        LocalDate birthDate = LocalDate.now().plusDays(1);
        assertFalse(validator.isValid(birthDate, context));
    }

    @Test
    @DisplayName("AC-DP-004: Deve rejeitar data de nascimento no futuro distante")
    void deveRejeitarDataNoFuturoDistante() {
        LocalDate birthDate = LocalDate.now().plusYears(1);
        assertFalse(validator.isValid(birthDate, context));
    }

    @Test
    @DisplayName("Deve aceitar null (validação de nulidade é separada)")
    void deveAceitarNull() {
        assertTrue(validator.isValid(null, context));
    }

    @Test
    @DisplayName("AC-DP-004: Deve rejeitar quase 13 anos (falta 1 dia)")
    void deveRejeitarQuase13Anos() {
        // Pessoa que vai fazer 13 anos amanhã
        LocalDate birthDate = LocalDate.now().minusYears(13).plusDays(1);
        assertFalse(validator.isValid(birthDate, context));
    }

    @Test
    @DisplayName("AC-DP-004: Deve aceitar pessoa que fez 13 anos hoje")
    void deveAceitarPessoaQueFez13AnosHoje() {
        LocalDate birthDate = LocalDate.now().minusYears(13);
        assertTrue(validator.isValid(birthDate, context));
    }

    @Test
    @DisplayName("AC-DP-004: Deve aceitar pessoa que fez 13 anos ontem")
    void deveAceitarPessoaQueFez13AnosOntem() {
        LocalDate birthDate = LocalDate.now().minusYears(13).minusDays(1);
        assertTrue(validator.isValid(birthDate, context));
    }

    // ========== Teste com idade mínima diferente ==========

    @Test
    @DisplayName("Deve validar com idade mínima customizada (18 anos)")
    void deveValidarComIdadeMinimaCustomizada() {
        // Reconfigura para idade mínima de 18
        MinAge minAge18 = mock(MinAge.class);
        when(minAge18.value()).thenReturn(18);
        validator.initialize(minAge18);

        // 17 anos - deve rejeitar
        LocalDate age17 = LocalDate.now().minusYears(17);
        assertFalse(validator.isValid(age17, context));

        // 18 anos - deve aceitar
        LocalDate age18 = LocalDate.now().minusYears(18);
        assertTrue(validator.isValid(age18, context));

        // 19 anos - deve aceitar
        LocalDate age19 = LocalDate.now().minusYears(19);
        assertTrue(validator.isValid(age19, context));
    }
}
