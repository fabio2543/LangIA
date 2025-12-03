package com.langia.backend.service;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.UUID;

import org.springframework.stereotype.Service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * Serviço para cálculo de hash SHA-1 para deduplicação de trilhas e conteúdo.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class TrailHashService {

    private final ObjectMapper objectMapper;

    /**
     * Calcula hash SHA-1 para uma trilha baseado nos parâmetros de entrada.
     * Usado para cache e deduplicação.
     *
     * @param studentId ID do estudante
     * @param languageCode Código do idioma
     * @param levelCode Código do nível CEFR
     * @param preferencesJson JSON das preferências do estudante
     * @param curriculumVersion Versão do currículo
     * @return Hash SHA-1 de 40 caracteres
     */
    public String calculateTrailHash(UUID studentId, String languageCode, String levelCode,
                                     String preferencesJson, String curriculumVersion) {
        String input = String.format("%s|%s|%s|%s|%s",
                studentId.toString(),
                languageCode,
                levelCode,
                preferencesJson != null ? preferencesJson : "{}",
                curriculumVersion);
        return sha1Hash(input);
    }

    /**
     * Calcula hash SHA-1 para um bloco de conteúdo.
     * Usado para deduplicação de content_blocks.
     *
     * @param descriptorId ID do descritor
     * @param languageCode Código do idioma
     * @param type Tipo de lição
     * @param content Conteúdo JSON
     * @return Hash SHA-1 de 40 caracteres
     */
    public String calculateContentHash(UUID descriptorId, String languageCode, String type, String content) {
        String input = String.format("%s|%s|%s|%s",
                descriptorId.toString(),
                languageCode,
                type,
                content);
        return sha1Hash(input);
    }

    /**
     * Calcula hash SHA-1 para um blueprint.
     *
     * @param languageCode Código do idioma
     * @param levelCode Código do nível
     * @param structure JSON da estrutura do blueprint
     * @return Hash SHA-1 de 40 caracteres
     */
    public String calculateBlueprintHash(String languageCode, String levelCode, String structure) {
        String input = String.format("%s|%s|%s", languageCode, levelCode, structure);
        return sha1Hash(input);
    }

    /**
     * Calcula hash SHA-1 a partir de um objeto Java.
     *
     * @param object Objeto a ser convertido para JSON e hashado
     * @return Hash SHA-1 de 40 caracteres
     */
    public String calculateObjectHash(Object object) {
        try {
            String json = objectMapper.writeValueAsString(object);
            return sha1Hash(json);
        } catch (JsonProcessingException e) {
            log.error("Erro ao serializar objeto para hash: {}", e.getMessage());
            throw new RuntimeException("Erro ao calcular hash do objeto", e);
        }
    }

    /**
     * Calcula hash SHA-1 de uma string.
     *
     * @param input String de entrada
     * @return Hash SHA-1 de 40 caracteres hexadecimais
     */
    private String sha1Hash(String input) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-1");
            byte[] hashBytes = md.digest(input.getBytes(StandardCharsets.UTF_8));
            return bytesToHex(hashBytes);
        } catch (NoSuchAlgorithmException e) {
            log.error("Algoritmo SHA-1 não disponível: {}", e.getMessage());
            throw new RuntimeException("SHA-1 não disponível", e);
        }
    }

    /**
     * Converte array de bytes para string hexadecimal.
     */
    private String bytesToHex(byte[] bytes) {
        StringBuilder sb = new StringBuilder();
        for (byte b : bytes) {
            sb.append(String.format("%02x", b));
        }
        return sb.toString();
    }
}
