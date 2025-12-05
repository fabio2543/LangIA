package com.langia.backend.service;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.Duration;
import java.util.HexFormat;
import java.util.function.Supplier;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import lombok.extern.slf4j.Slf4j;

/**
 * Serviço de cache Redis para respostas do Gemini.
 * Evita chamadas repetidas ao LLM para prompts/textos idênticos.
 */
@Service
@Slf4j
public class GeminiCacheService {

    private static final String CONTENT_CACHE_PREFIX = "gemini:content:";
    private static final String EMBEDDING_CACHE_PREFIX = "gemini:embedding:";

    private final RedisTemplate<String, String> redisTemplate;

    @Value("${gemini.cache.enabled:true}")
    private boolean cacheEnabled;

    @Value("${gemini.cache.content.ttl-hours:24}")
    private long contentTtlHours;

    @Value("${gemini.cache.embedding.ttl-days:30}")
    private long embeddingTtlDays;

    public GeminiCacheService(RedisTemplate<String, String> redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    /**
     * Obtém conteúdo do cache ou gera novo via supplier.
     *
     * @param prompt Prompt enviado ao Gemini
     * @param generator Função que gera o conteúdo se não estiver em cache
     * @return Conteúdo do cache ou gerado
     */
    public String getOrGenerateContent(String prompt, Supplier<String> generator) {
        if (!cacheEnabled) {
            return generator.get();
        }

        String cacheKey = CONTENT_CACHE_PREFIX + hashText(prompt);

        try {
            String cached = redisTemplate.opsForValue().get(cacheKey);
            if (cached != null) {
                log.debug("Cache HIT para content: {}", cacheKey.substring(0, Math.min(50, cacheKey.length())));
                return cached;
            }
        } catch (Exception e) {
            log.warn("Erro ao ler cache de content: {}", e.getMessage());
        }

        String result = generator.get();

        try {
            Duration ttl = Duration.ofHours(contentTtlHours);
            redisTemplate.opsForValue().set(cacheKey, result, ttl);
            log.debug("Cache MISS - Armazenado content com TTL {}h", contentTtlHours);
        } catch (Exception e) {
            log.warn("Erro ao salvar cache de content: {}", e.getMessage());
        }

        return result;
    }

    /**
     * Obtém embedding do cache ou gera novo via supplier.
     *
     * @param text Texto para gerar embedding
     * @param generator Função que gera o embedding se não estiver em cache
     * @return Embedding em formato JSON string
     */
    public String getOrGenerateEmbedding(String text, Supplier<String> generator) {
        if (!cacheEnabled) {
            return generator.get();
        }

        String cacheKey = EMBEDDING_CACHE_PREFIX + hashText(text);

        try {
            String cached = redisTemplate.opsForValue().get(cacheKey);
            if (cached != null) {
                log.debug("Cache HIT para embedding: {}", cacheKey.substring(0, Math.min(50, cacheKey.length())));
                return cached;
            }
        } catch (Exception e) {
            log.warn("Erro ao ler cache de embedding: {}", e.getMessage());
        }

        String result = generator.get();

        try {
            Duration ttl = Duration.ofDays(embeddingTtlDays);
            redisTemplate.opsForValue().set(cacheKey, result, ttl);
            log.debug("Cache MISS - Armazenado embedding com TTL {}d", embeddingTtlDays);
        } catch (Exception e) {
            log.warn("Erro ao salvar cache de embedding: {}", e.getMessage());
        }

        return result;
    }

    /**
     * Invalida cache de conteúdo para um prompt específico.
     *
     * @param prompt Prompt para invalidar
     */
    public void invalidateContentCache(String prompt) {
        String cacheKey = CONTENT_CACHE_PREFIX + hashText(prompt);
        try {
            redisTemplate.delete(cacheKey);
            log.debug("Cache invalidado para content: {}", cacheKey);
        } catch (Exception e) {
            log.warn("Erro ao invalidar cache: {}", e.getMessage());
        }
    }

    /**
     * Invalida cache de embedding para um texto específico.
     *
     * @param text Texto para invalidar
     */
    public void invalidateEmbeddingCache(String text) {
        String cacheKey = EMBEDDING_CACHE_PREFIX + hashText(text);
        try {
            redisTemplate.delete(cacheKey);
            log.debug("Cache invalidado para embedding: {}", cacheKey);
        } catch (Exception e) {
            log.warn("Erro ao invalidar cache: {}", e.getMessage());
        }
    }

    /**
     * Gera hash SHA-256 do texto para usar como chave de cache.
     *
     * @param text Texto para gerar hash
     * @return Hash em hexadecimal
     */
    private String hashText(String text) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(text.getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(hash);
        } catch (NoSuchAlgorithmException e) {
            // SHA-256 sempre disponível em Java
            throw new RuntimeException("SHA-256 não disponível", e);
        }
    }
}
