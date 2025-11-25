package com.langia.backend.util;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * Testes para validação de CPF brasileiro.
 * Valida algoritmo completo incluindo dígitos verificadores.
 */
class CpfValidatorTest {

    private CpfValidator cpfValidator;

    @BeforeEach
    void setUp() {
        cpfValidator = new CpfValidator();
    }

    // Testes com CPFs válidos
    @Test
    void deveValidarCpfValidoSemFormatacao() {
        assertTrue(cpfValidator.isValid("11144477735"), "CPF válido sem formatação deveria ser aceito");
    }

    @Test
    void deveValidarCpfValidoComFormatacao() {
        assertTrue(cpfValidator.isValid("111.444.777-35"), "CPF válido com formatação deveria ser aceito");
    }

    @Test
    void deveValidarMultiplosCpfsValidos() {
        // CPFs válidos conhecidos (validados pelo algoritmo)
        String[] cpfsValidos = {
                "111.444.777-35",
                "11144477735",
                "529.982.247-25",
                "52998224725"
        };

        for (String cpf : cpfsValidos) {
            assertTrue(cpfValidator.isValid(cpf), "CPF válido não foi aceito: " + cpf);
        }
    }

    // Testes com CPFs inválidos - formato
    @Test
    void deveRejeitarCpfNulo() {
        assertFalse(cpfValidator.isValid(null), "CPF nulo deveria ser rejeitado");
    }

    @Test
    void deveRejeitarCpfVazio() {
        assertFalse(cpfValidator.isValid(""), "CPF vazio deveria ser rejeitado");
        assertFalse(cpfValidator.isValid("   "), "CPF com espaços deveria ser rejeitado");
    }

    @Test
    void deveRejeitarCpfComMenosDe11Digitos() {
        assertFalse(cpfValidator.isValid("123456789"), "CPF com 9 dígitos deveria ser rejeitado");
        assertFalse(cpfValidator.isValid("12345678901"), "CPF com 11 dígitos mas incompleto deveria ser rejeitado");
    }

    @Test
    void deveRejeitarCpfComMaisDe11Digitos() {
        assertFalse(cpfValidator.isValid("123456789012"), "CPF com 12 dígitos deveria ser rejeitado");
    }

    @Test
    void deveRejeitarCpfComCaracteresNaoNumericos() {
        assertFalse(cpfValidator.isValid("111.444.777-3a"), "CPF com letra deveria ser rejeitado");
        assertFalse(cpfValidator.isValid("abc.def.ghi-jk"), "CPF com letras deveria ser rejeitado");
    }

    // Testes com CPFs inválidos - sequências repetidas
    @Test
    void deveRejeitarCpfsComSequenciasRepetidas() {
        String[] cpfsInvalidos = {
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
        };

        for (String cpf : cpfsInvalidos) {
            assertFalse(cpfValidator.isValid(cpf), "CPF com sequência repetida deveria ser rejeitado: " + cpf);
        }
    }

    @Test
    void deveRejeitarCpfsComSequenciasRepetidasFormatados() {
        assertFalse(cpfValidator.isValid("111.111.111-11"), "CPF 111.111.111-11 deveria ser rejeitado");
        assertFalse(cpfValidator.isValid("000.000.000-00"), "CPF 000.000.000-00 deveria ser rejeitado");
    }

    // Testes com CPFs inválidos - dígitos verificadores incorretos
    @Test
    void deveRejeitarCpfComPrimeiroDigitoVerificadorIncorreto() {
        assertFalse(cpfValidator.isValid("11144477745"), "CPF com primeiro dígito incorreto deveria ser rejeitado");
    }

    @Test
    void deveRejeitarCpfComSegundoDigitoVerificadorIncorreto() {
        assertFalse(cpfValidator.isValid("11144477736"), "CPF com segundo dígito incorreto deveria ser rejeitado");
    }

    @Test
    void deveRejeitarCpfComAmbosDigitosVerificadoresIncorretos() {
        assertFalse(cpfValidator.isValid("11144477700"), "CPF com ambos dígitos incorretos deveria ser rejeitado");
    }

    // Testes de limpeza de CPF
    @Test
    void deveLimparCpfComFormatacao() {
        assertEquals("11144477735", cpfValidator.cleanCpf("111.444.777-35"));
    }

    @Test
    void deveLimparCpfComEspacos() {
        assertEquals("11144477735", cpfValidator.cleanCpf("111 444 777 35"));
    }

    @Test
    void deveLimparCpfComMixDeFormatacao() {
        assertEquals("11144477735", cpfValidator.cleanCpf("111.444.777 - 35"));
    }

    @Test
    void deveLimparCpfNulo() {
        assertEquals("", cpfValidator.cleanCpf(null));
    }

    @Test
    void deveLimparCpfJaLimpo() {
        assertEquals("11144477735", cpfValidator.cleanCpf("11144477735"));
    }

    // Testes de formatação
    @Test
    void deveFormatarCpfLimpo() {
        assertEquals("111.444.777-35", cpfValidator.formatCpf("11144477735"));
    }

    @Test
    void deveFormatarCpfAposLimpar() {
        String cpfFormatado = "111.444.777-35";
        String cpfLimpo = cpfValidator.cleanCpf(cpfFormatado);
        assertEquals("111.444.777-35", cpfValidator.formatCpf(cpfLimpo));
    }

    @Test
    void deveRetornarVazioParaCpfInvalidoNaFormatacao() {
        assertEquals("", cpfValidator.formatCpf("123"));
        assertEquals("", cpfValidator.formatCpf(null));
        assertEquals("", cpfValidator.formatCpf(""));
    }

    // Testes de casos específicos
    @Test
    void deveValidarCpfComDigitosVariados() {
        // CPF válido com dígitos variados
        assertTrue(cpfValidator.isValid("529.982.247-25"));
    }

    @Test
    void deveValidarCpfSemFormatacaoComDigitosVariados() {
        // CPF válido sem formatação
        assertTrue(cpfValidator.isValid("52998224725"));
    }

    @Test
    void deveRejeitarCpfQuaseValido() {
        // CPF que difere por apenas 1 dígito de um CPF válido
        assertFalse(cpfValidator.isValid("11144477734")); // Último dígito errado
        assertFalse(cpfValidator.isValid("11144477755")); // Último dígito errado
    }

    // Testes de edge cases
    @Test
    void deveRejeitarCpfComApenasZeros() {
        assertFalse(cpfValidator.isValid("00000000000"));
    }

    @Test
    void deveRejeitarCpfComCaracteresEspeciais() {
        assertFalse(cpfValidator.isValid("111@444#777$35"));
    }

    @Test
    void deveValidarCpfComEspacosEFormatacao() {
        assertTrue(cpfValidator.isValid(" 111.444.777-35 "), "CPF válido com espaços nas pontas deveria ser aceito após limpeza");
    }

    // Teste de integração - validação completa
    @Test
    void deveValidarFluxoCompletoDeValidacao() {
        String cpfComFormatacao = "111.444.777-35";

        // Limpa
        String cpfLimpo = cpfValidator.cleanCpf(cpfComFormatacao);
        assertEquals("11144477735", cpfLimpo);

        // Valida
        assertTrue(cpfValidator.isValid(cpfLimpo));

        // Formata novamente
        String cpfFormatado = cpfValidator.formatCpf(cpfLimpo);
        assertEquals("111.444.777-35", cpfFormatado);
    }

    // Teste de performance
    @Test
    void deveValidarRapidamenteMultiplosCpfs() {
        long startTime = System.currentTimeMillis();

        for (int i = 0; i < 1000; i++) {
            cpfValidator.isValid("111.444.777-35");
        }

        long endTime = System.currentTimeMillis();
        long duration = endTime - startTime;

        assertTrue(duration < 1000, "Validação de 1000 CPFs deveria levar menos de 1 segundo");
    }
}
