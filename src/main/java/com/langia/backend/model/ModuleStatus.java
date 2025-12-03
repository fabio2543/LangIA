package com.langia.backend.model;

/**
 * Status de um módulo dentro de uma trilha.
 * Corresponde ao enum module_status no banco de dados.
 */
public enum ModuleStatus {
    /**
     * Módulo aguardando geração de conteúdo
     */
    PENDING,

    /**
     * Módulo com todas as lições geradas e disponíveis
     */
    READY
}
