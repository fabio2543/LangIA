package com.langia.backend.model;

/**
 * Status de uma trilha de aprendizado.
 * Corresponde ao enum trail_status no banco de dados.
 */
public enum TrailStatus {
    /**
     * Trilha está sendo gerada pela IA (processo em andamento)
     */
    GENERATING,

    /**
     * Alguns módulos prontos, outros ainda em geração (permite acesso parcial)
     */
    PARTIAL,

    /**
     * Trilha completamente gerada e disponível para o estudante
     */
    READY,

    /**
     * Trilha arquivada (substituída por versão mais recente ou inativa)
     */
    ARCHIVED
}
