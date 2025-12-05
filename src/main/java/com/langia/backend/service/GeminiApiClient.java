package com.langia.backend.service;

import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import lombok.extern.slf4j.Slf4j;

/**
 * Cliente REST para API direta do Google Gemini.
 * Usa API key via header (x-goog-api-key) para segurança.
 * Suporta geração de conteúdo e embeddings.
 */
@Service
@Slf4j
public class GeminiApiClient {

    private static final String GEMINI_CONTENT_URL = "https://generativelanguage.googleapis.com/v1beta/models/gemini-2.0-flash:generateContent";
    private static final String GEMINI_EMBEDDING_URL = "https://generativelanguage.googleapis.com/v1beta/models/text-embedding-004:embedContent";

    @Value("${gemini.api-key:}")
    private String apiKey;

    @Value("${embedding.dimensions:768}")
    private int embeddingDimensions;

    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;
    private final GeminiCacheService cacheService;

    @Autowired
    public GeminiApiClient(ObjectMapper objectMapper, GeminiCacheService cacheService) {
        this.restTemplate = new RestTemplate();
        this.objectMapper = objectMapper;
        this.cacheService = cacheService;
    }

    /**
     * Verifica se a API key está configurada.
     */
    public boolean isConfigured() {
        return apiKey != null && !apiKey.isBlank();
    }

    /**
     * Envia prompt para o Gemini e retorna a resposta.
     * Usa cache Redis para evitar chamadas repetidas.
     *
     * @param prompt O prompt a ser enviado
     * @return Resposta do modelo
     */
    public String generateContent(String prompt) {
        if (!isConfigured()) {
            log.warn("GEMINI_API_KEY não configurada");
            throw new RuntimeException("GEMINI_API_KEY não configurada");
        }

        return cacheService.getOrGenerateContent(prompt, () -> doGenerateContent(prompt));
    }

    /**
     * Gera embedding para um texto usando Gemini text-embedding-004.
     * Usa cache Redis para evitar chamadas repetidas.
     *
     * @param text Texto para gerar embedding
     * @return Array de floats com o embedding (768 dimensões)
     */
    public float[] generateEmbedding(String text) {
        if (!isConfigured()) {
            log.warn("GEMINI_API_KEY não configurada");
            throw new RuntimeException("GEMINI_API_KEY não configurada");
        }

        String embeddingJson = cacheService.getOrGenerateEmbedding(text, () -> doGenerateEmbedding(text));
        return parseEmbeddingFromJson(embeddingJson);
    }

    /**
     * Executa chamada real à API de geração de conteúdo.
     */
    private String doGenerateContent(String prompt) {
        try {
            HttpHeaders headers = createHeaders();

            Map<String, Object> requestBody = Map.of(
                "contents", List.of(
                    Map.of(
                        "parts", List.of(
                            Map.of("text", prompt)
                        )
                    )
                ),
                "generationConfig", Map.of(
                    "temperature", 0.7,
                    "maxOutputTokens", 8192,
                    "topP", 0.95
                )
            );

            String requestJson = objectMapper.writeValueAsString(requestBody);
            HttpEntity<String> entity = new HttpEntity<>(requestJson, headers);

            log.debug("Enviando request para Gemini Content API...");
            String response = restTemplate.postForObject(GEMINI_CONTENT_URL, entity, String.class);

            return extractTextFromResponse(response);

        } catch (Exception e) {
            log.error("Erro ao chamar Gemini Content API: {}", e.getMessage(), e);
            throw new RuntimeException("Falha ao gerar conteúdo via Gemini: " + e.getMessage(), e);
        }
    }

    /**
     * Executa chamada real à API de embeddings.
     * Retorna JSON string do embedding para cache.
     */
    private String doGenerateEmbedding(String text) {
        try {
            HttpHeaders headers = createHeaders();

            Map<String, Object> requestBody = Map.of(
                "model", "models/text-embedding-004",
                "content", Map.of(
                    "parts", List.of(
                        Map.of("text", text)
                    )
                )
            );

            String requestJson = objectMapper.writeValueAsString(requestBody);
            HttpEntity<String> entity = new HttpEntity<>(requestJson, headers);

            log.debug("Enviando request para Gemini Embedding API...");
            String response = restTemplate.postForObject(GEMINI_EMBEDDING_URL, entity, String.class);

            // Retorna o JSON bruto do embedding para ser cacheado
            return extractEmbeddingJson(response);

        } catch (Exception e) {
            log.error("Erro ao chamar Gemini Embedding API: {}", e.getMessage(), e);
            throw new RuntimeException("Falha ao gerar embedding via Gemini: " + e.getMessage(), e);
        }
    }

    /**
     * Cria headers padrão para requisições ao Gemini.
     * API key é enviada via header x-goog-api-key (mais seguro que query string).
     */
    private HttpHeaders createHeaders() {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("x-goog-api-key", apiKey);
        return headers;
    }

    /**
     * Extrai o texto da resposta da API de geração de conteúdo.
     */
    private String extractTextFromResponse(String response) {
        try {
            JsonNode root = objectMapper.readTree(response);
            JsonNode candidates = root.path("candidates");

            if (candidates.isArray() && !candidates.isEmpty()) {
                JsonNode content = candidates.get(0).path("content");
                JsonNode parts = content.path("parts");

                if (parts.isArray() && !parts.isEmpty()) {
                    return parts.get(0).path("text").asText();
                }
            }

            log.warn("Resposta do Gemini sem conteúdo válido: {}", response);
            return "";

        } catch (Exception e) {
            log.error("Erro ao parsear resposta do Gemini: {}", e.getMessage());
            throw new RuntimeException("Erro ao parsear resposta: " + e.getMessage(), e);
        }
    }

    /**
     * Extrai o array de valores do embedding como JSON string.
     * Formato da resposta: { "embedding": { "values": [...] } }
     */
    private String extractEmbeddingJson(String response) {
        try {
            JsonNode root = objectMapper.readTree(response);
            JsonNode values = root.path("embedding").path("values");

            if (values.isArray() && !values.isEmpty()) {
                return values.toString();
            }

            log.warn("Resposta do Gemini Embedding sem valores válidos: {}", response);
            return "[]";

        } catch (Exception e) {
            log.error("Erro ao parsear resposta de embedding: {}", e.getMessage());
            throw new RuntimeException("Erro ao parsear embedding: " + e.getMessage(), e);
        }
    }

    /**
     * Converte JSON string de embedding para array de floats.
     */
    private float[] parseEmbeddingFromJson(String embeddingJson) {
        try {
            JsonNode values = objectMapper.readTree(embeddingJson);

            if (!values.isArray()) {
                log.warn("Embedding JSON inválido, retornando array vazio");
                return new float[embeddingDimensions];
            }

            float[] embedding = new float[values.size()];
            for (int i = 0; i < values.size(); i++) {
                embedding[i] = (float) values.get(i).asDouble();
            }

            return embedding;

        } catch (Exception e) {
            log.error("Erro ao converter embedding JSON para float[]: {}", e.getMessage());
            return new float[embeddingDimensions];
        }
    }
}
