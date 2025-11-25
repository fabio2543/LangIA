package com.langia.backend.util;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * Testes para validação de telefone brasileiro.
 * Valida celular, fixo, DDDs e formatações.
 */
class PhoneValidatorTest {

    private PhoneValidator phoneValidator;

    @BeforeEach
    void setUp() {
        phoneValidator = new PhoneValidator();
    }

    // Testes com celulares válidos
    @Test
    void deveValidarCelularValidoSemFormatacao() {
        assertTrue(phoneValidator.isValid("11987654321"), "Celular válido sem formatação deveria ser aceito");
    }

    @Test
    void deveValidarCelularValidoComFormatacao() {
        assertTrue(phoneValidator.isValid("(11) 98765-4321"), "Celular válido com formatação deveria ser aceito");
    }

    @Test
    void deveValidarCelularComCodigoPais() {
        assertTrue(phoneValidator.isValid("+55 11 98765-4321"), "Celular com código do país deveria ser aceito");
        assertTrue(phoneValidator.isValid("+5511987654321"), "Celular com código do país sem formatação deveria ser aceito");
    }

    @Test
    void deveValidarCelularesDeTodasRegioes() {
        String[] celularesValidos = {
                "11987654321", // São Paulo
                "21987654321", // Rio de Janeiro
                "85987654321", // Ceará
                "47987654321", // Santa Catarina
                "61987654321", // Distrito Federal
                "71987654321", // Bahia
                "91987654321"  // Pará
        };

        for (String celular : celularesValidos) {
            assertTrue(phoneValidator.isValid(celular), "Celular válido não foi aceito: " + celular);
        }
    }

    // Testes com telefones fixos válidos
    @Test
    void deveValidarFixoValidoSemFormatacao() {
        assertTrue(phoneValidator.isValid("1134567890"), "Fixo válido sem formatação deveria ser aceito");
    }

    @Test
    void deveValidarFixoValidoComFormatacao() {
        assertTrue(phoneValidator.isValid("(11) 3456-7890"), "Fixo válido com formatação deveria ser aceito");
    }

    @Test
    void deveValidarFixosDeTodasRegioes() {
        String[] fixosValidos = {
                "1134567890", // São Paulo
                "2134567890", // Rio de Janeiro
                "8534567890", // Ceará
                "4734567890", // Santa Catarina
                "6134567890", // Distrito Federal
                "7134567890", // Bahia
                "9134567890"  // Pará
        };

        for (String fixo : fixosValidos) {
            assertTrue(phoneValidator.isValid(fixo), "Fixo válido não foi aceito: " + fixo);
        }
    }

    // Testes com telefones inválidos - formato
    @Test
    void deveRejeitarTelefoneNulo() {
        assertFalse(phoneValidator.isValid(null), "Telefone nulo deveria ser rejeitado");
    }

    @Test
    void deveRejeitarTelefoneVazio() {
        assertFalse(phoneValidator.isValid(""), "Telefone vazio deveria ser rejeitado");
        assertFalse(phoneValidator.isValid("   "), "Telefone com espaços deveria ser rejeitado");
    }

    @Test
    void deveRejeitarTelefoneComMenosDe10Digitos() {
        assertFalse(phoneValidator.isValid("119876543"), "Telefone com 9 dígitos deveria ser rejeitado");
    }

    @Test
    void deveRejeitarTelefoneComMaisDe11Digitos() {
        assertFalse(phoneValidator.isValid("119876543210"), "Telefone com 12 dígitos deveria ser rejeitado");
    }

    @Test
    void deveRejeitarTelefoneComCaracteresNaoNumericos() {
        assertFalse(phoneValidator.isValid("11 9876a-4321"), "Telefone com letra deveria ser rejeitado");
    }

    // Testes com DDDs inválidos
    @Test
    void deveRejeitarDDDInexistente() {
        assertFalse(phoneValidator.isValid("10987654321"), "DDD 10 não existe");
        assertFalse(phoneValidator.isValid("00987654321"), "DDD 00 não existe");
        assertFalse(phoneValidator.isValid("20987654321"), "DDD 20 não existe");
        assertFalse(phoneValidator.isValid("90987654321"), "DDD 90 não existe");
    }

    // Testes com celular inválido
    @Test
    void deveRejeitarCelularSemDigito9() {
        assertFalse(phoneValidator.isValid("11887654321"), "Celular deve começar com 9 após o DDD");
    }

    @Test
    void deveAceitarNumero10DigitosComoFixo() {
        // 1187654321 é um número fixo válido (não começa com 9)
        assertTrue(phoneValidator.isValid("1187654321"), "Número com 10 dígitos iniciado com 8 é fixo válido");
        assertTrue(phoneValidator.isLandline("1187654321"));
    }

    // Testes com fixo inválido
    @Test
    void deveRejeitarFixoComDigito9() {
        assertFalse(phoneValidator.isValid("1194567890"), "Fixo não deve começar com 9 após o DDD");
    }

    @Test
    void deveRejeitarFixoCom11Digitos() {
        assertFalse(phoneValidator.isValid("11834567890"), "Fixo com 11 dígitos deveria ser rejeitado");
    }

    // Testes de identificação de tipo
    @Test
    void deveIdentificarCelular() {
        assertTrue(phoneValidator.isMobile("11987654321"));
        assertTrue(phoneValidator.isMobile("(11) 98765-4321"));
        assertFalse(phoneValidator.isMobile("1134567890"));
    }

    @Test
    void deveIdentificarFixo() {
        assertTrue(phoneValidator.isLandline("1134567890"));
        assertTrue(phoneValidator.isLandline("(11) 3456-7890"));
        assertFalse(phoneValidator.isLandline("11987654321"));
    }

    // Testes de limpeza
    @Test
    void deveLimparTelefoneComFormatacao() {
        assertEquals("11987654321", phoneValidator.cleanPhone("(11) 98765-4321"));
    }

    @Test
    void deveLimparTelefoneComCodigoPais() {
        assertEquals("11987654321", phoneValidator.cleanPhone("+55 11 98765-4321"));
        assertEquals("11987654321", phoneValidator.cleanPhone("+5511987654321"));
    }

    @Test
    void deveLimparTelefoneComEspacos() {
        assertEquals("11987654321", phoneValidator.cleanPhone("11 98765 4321"));
    }

    @Test
    void deveLimparTelefoneNulo() {
        assertEquals("", phoneValidator.cleanPhone(null));
    }

    @Test
    void deveLimparTelefoneJaLimpo() {
        assertEquals("11987654321", phoneValidator.cleanPhone("11987654321"));
    }

    // Testes de formatação para WhatsApp
    @Test
    void deveFormatarCelularParaWhatsApp() {
        assertEquals("+5511987654321", phoneValidator.formatForWhatsApp("11987654321"));
        assertEquals("+5511987654321", phoneValidator.formatForWhatsApp("(11) 98765-4321"));
        assertEquals("+5511987654321", phoneValidator.formatForWhatsApp("+55 11 98765-4321"));
    }

    @Test
    void deveFormatarFixoParaWhatsApp() {
        assertEquals("+551134567890", phoneValidator.formatForWhatsApp("1134567890"));
        assertEquals("+551134567890", phoneValidator.formatForWhatsApp("(11) 3456-7890"));
    }

    @Test
    void deveRetornarVazioParaTelefoneInvalidoNoWhatsApp() {
        assertEquals("", phoneValidator.formatForWhatsApp("invalid"));
        assertEquals("", phoneValidator.formatForWhatsApp(null));
    }

    // Testes de formatação para exibição
    @Test
    void deveFormatarCelularParaExibicao() {
        assertEquals("(11) 98765-4321", phoneValidator.formatForDisplay("11987654321"));
        assertEquals("(11) 98765-4321", phoneValidator.formatForDisplay("(11) 98765-4321"));
    }

    @Test
    void deveFormatarFixoParaExibicao() {
        assertEquals("(11) 3456-7890", phoneValidator.formatForDisplay("1134567890"));
        assertEquals("(11) 3456-7890", phoneValidator.formatForDisplay("(11) 3456-7890"));
    }

    @Test
    void deveRetornarVazioParaTelefoneInvalidoNaExibicao() {
        assertEquals("", phoneValidator.formatForDisplay("invalid"));
        assertEquals("", phoneValidator.formatForDisplay(null));
    }

    // Testes de extração de DDD
    @Test
    void deveExtrairDDD() {
        assertEquals("11", phoneValidator.extractDDD("11987654321"));
        assertEquals("11", phoneValidator.extractDDD("(11) 98765-4321"));
        assertEquals("85", phoneValidator.extractDDD("+55 85 98765-4321"));
    }

    @Test
    void deveRetornarVazioParaDDDInvalido() {
        assertEquals("", phoneValidator.extractDDD("invalid"));
        assertEquals("", phoneValidator.extractDDD(null));
        assertEquals("", phoneValidator.extractDDD("123"));
    }

    // Testes de validação de DDD
    @Test
    void deveValidarDDDsExistentes() {
        // Região Sudeste
        assertTrue(phoneValidator.isValidDDD("11")); // São Paulo
        assertTrue(phoneValidator.isValidDDD("21")); // Rio de Janeiro
        assertTrue(phoneValidator.isValidDDD("31")); // Minas Gerais

        // Região Sul
        assertTrue(phoneValidator.isValidDDD("41")); // Paraná
        assertTrue(phoneValidator.isValidDDD("47")); // Santa Catarina
        assertTrue(phoneValidator.isValidDDD("51")); // Rio Grande do Sul

        // Região Centro-Oeste
        assertTrue(phoneValidator.isValidDDD("61")); // Distrito Federal
        assertTrue(phoneValidator.isValidDDD("65")); // Mato Grosso

        // Região Nordeste
        assertTrue(phoneValidator.isValidDDD("71")); // Bahia
        assertTrue(phoneValidator.isValidDDD("81")); // Pernambuco
        assertTrue(phoneValidator.isValidDDD("85")); // Ceará

        // Região Norte
        assertTrue(phoneValidator.isValidDDD("91")); // Pará
        assertTrue(phoneValidator.isValidDDD("92")); // Amazonas
    }

    @Test
    void deveRejeitarDDDsInexistentes() {
        assertFalse(phoneValidator.isValidDDD("00"));
        assertFalse(phoneValidator.isValidDDD("10"));
        assertFalse(phoneValidator.isValidDDD("20"));
        assertFalse(phoneValidator.isValidDDD("90"));
    }

    // Testes de casos específicos
    @Test
    void deveValidarCelularComDiferentesDDDs() {
        // Testa DDDs de diferentes estados
        String[] dddsValidos = {"11", "21", "31", "41", "51", "61", "71", "81", "85", "91"};

        for (String ddd : dddsValidos) {
            String celular = ddd + "987654321";
            assertTrue(phoneValidator.isValid(celular), "Celular com DDD " + ddd + " deveria ser válido");
        }
    }

    @Test
    void deveValidarTelefoneComFormatacoesDiversas() {
        String[] formatos = {
                "11987654321",
                "(11) 98765-4321",
                "(11)98765-4321",
                "11 98765-4321",
                "+55 11 98765-4321",
                "+5511987654321",
                "+55(11)98765-4321"
        };

        for (String formato : formatos) {
            assertTrue(phoneValidator.isValid(formato), "Formato deveria ser aceito: " + formato);
        }
    }

    // Teste de integração
    @Test
    void deveValidarFluxoCompleto() {
        String telefoneFormatado = "(11) 98765-4321";

        // Limpa
        String telefoneLimpo = phoneValidator.cleanPhone(telefoneFormatado);
        assertEquals("11987654321", telefoneLimpo);

        // Valida
        assertTrue(phoneValidator.isValid(telefoneLimpo));

        // Identifica tipo
        assertTrue(phoneValidator.isMobile(telefoneLimpo));

        // Formata para WhatsApp
        String whatsapp = phoneValidator.formatForWhatsApp(telefoneLimpo);
        assertEquals("+5511987654321", whatsapp);

        // Formata para exibição
        String exibicao = phoneValidator.formatForDisplay(telefoneLimpo);
        assertEquals("(11) 98765-4321", exibicao);

        // Extrai DDD
        String ddd = phoneValidator.extractDDD(telefoneLimpo);
        assertEquals("11", ddd);
    }
}
