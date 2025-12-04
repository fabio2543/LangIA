package com.langia.backend.model;

/**
 * Tipo de fonte para embeddings RAG.
 * Corresponde ao check constraint chk_rag_source_type na tabela rag_embeddings.
 */
public enum RagSourceType {
    /**
     * Bloco de conteúdo gerado
     */
    content_block,

    /**
     * Descritor do currículo
     */
    descriptor,

    /**
     * Lição de uma trilha
     */
    lesson,

    /**
     * Módulo de uma trilha
     */
    module,

    /**
     * Trilha completa
     */
    trail
}
