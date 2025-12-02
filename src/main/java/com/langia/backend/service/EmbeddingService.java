package com.langia.backend.service;

import java.util.Arrays;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import org.springframework.ai.embedding.EmbeddingModel;
import org.springframework.ai.embedding.EmbeddingResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.langia.backend.model.DocumentEmbedding;
import com.langia.backend.repository.DocumentEmbeddingRepository;

import lombok.extern.slf4j.Slf4j;

/**
 * Serviço para gerar embeddings e realizar buscas semânticas.
 */
@Service
@Slf4j
public class EmbeddingService {

    private final DocumentEmbeddingRepository documentRepository;
    private final EmbeddingModel embeddingModel;

    @Value("${embedding.dimensions:768}")
    private int embeddingDimensions;

    @Autowired
    public EmbeddingService(DocumentEmbeddingRepository documentRepository,
            @Autowired(required = false) EmbeddingModel embeddingModel) {
        this.documentRepository = documentRepository;
        this.embeddingModel = embeddingModel;
    }

    /**
     * Gera embedding para um texto usando Gemini.
     *
     * @param text texto para gerar embedding
     * @return array de floats representando o embedding
     */
    public float[] generateEmbedding(String text) {
        if (embeddingModel == null) {
            log.warn("EmbeddingModel não configurado. Retornando embedding vazio.");
            return new float[embeddingDimensions];
        }

        try {
            EmbeddingResponse response = embeddingModel.embedForResponse(List.of(text));
            return response.getResult().getOutput();
        } catch (Exception e) {
            log.error("Erro ao gerar embedding: {}", e.getMessage());
            throw new RuntimeException("Falha ao gerar embedding", e);
        }
    }

    /**
     * Salva um documento com seu embedding.
     *
     * @param content     conteúdo do documento
     * @param contentType tipo de conteúdo (ex: "lesson", "faq", "guide")
     * @param metadata    metadados em formato JSON
     * @return documento salvo
     */
    @Transactional
    public DocumentEmbedding saveDocument(String content, String contentType, String metadata) {
        float[] embedding = generateEmbedding(content);

        DocumentEmbedding document = DocumentEmbedding.builder()
                .content(content)
                .contentType(contentType)
                .metadata(metadata)
                .embedding(embedding)
                .build();

        return documentRepository.save(document);
    }

    /**
     * Busca documentos semanticamente similares.
     *
     * @param query       texto da query
     * @param contentType tipo de conteúdo para filtrar (null para todos)
     * @param limit       número máximo de resultados
     * @return lista de documentos similares
     */
    @Transactional(readOnly = true)
    public List<DocumentEmbedding> findSimilar(String query, String contentType, int limit) {
        float[] queryEmbedding = generateEmbedding(query);
        String vectorString = floatArrayToVectorString(queryEmbedding);

        return documentRepository.findSimilar(vectorString, contentType, limit);
    }

    /**
     * Busca documentos similares com threshold de distância.
     *
     * @param query       texto da query
     * @param contentType tipo de conteúdo
     * @param maxDistance distância máxima (0.0 a 2.0)
     * @param limit       número máximo de resultados
     * @return lista de documentos
     */
    @Transactional(readOnly = true)
    public List<DocumentEmbedding> findSimilarWithThreshold(String query, String contentType, double maxDistance,
            int limit) {
        float[] queryEmbedding = generateEmbedding(query);
        String vectorString = floatArrayToVectorString(queryEmbedding);

        return documentRepository.findSimilarWithThreshold(vectorString, contentType, maxDistance, limit);
    }

    /**
     * Atualiza o embedding de um documento existente.
     *
     * @param documentId ID do documento
     * @return documento atualizado
     */
    @Transactional
    public DocumentEmbedding updateEmbedding(UUID documentId) {
        DocumentEmbedding document = documentRepository.findById(documentId)
                .orElseThrow(() -> new RuntimeException("Documento não encontrado: " + documentId));

        float[] newEmbedding = generateEmbedding(document.getContent());
        document.setEmbedding(newEmbedding);

        return documentRepository.save(document);
    }

    /**
     * Remove um documento.
     *
     * @param documentId ID do documento
     */
    @Transactional
    public void deleteDocument(UUID documentId) {
        documentRepository.deleteById(documentId);
    }

    /**
     * Obtém documento por ID.
     *
     * @param documentId ID do documento
     * @return documento ou null
     */
    @Transactional(readOnly = true)
    public DocumentEmbedding getDocument(UUID documentId) {
        return documentRepository.findById(documentId).orElse(null);
    }

    /**
     * Lista documentos por tipo.
     *
     * @param contentType tipo de conteúdo
     * @return lista de documentos
     */
    @Transactional(readOnly = true)
    public List<DocumentEmbedding> listByType(String contentType) {
        return documentRepository.findByContentType(contentType);
    }

    /**
     * Converte array de floats para formato de vetor do PostgreSQL.
     * Ex: [0.1, 0.2, 0.3] -> "[0.1,0.2,0.3]"
     */
    private String floatArrayToVectorString(float[] array) {
        return "[" + Arrays.stream(boxFloatArray(array))
                .map(String::valueOf)
                .collect(Collectors.joining(",")) + "]";
    }

    /**
     * Converte float[] primitivo para Float[] boxed.
     */
    private Float[] boxFloatArray(float[] array) {
        Float[] boxed = new Float[array.length];
        for (int i = 0; i < array.length; i++) {
            boxed[i] = array[i];
        }
        return boxed;
    }
}
