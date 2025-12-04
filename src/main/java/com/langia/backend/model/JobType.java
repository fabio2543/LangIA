package com.langia.backend.model;

/**
 * Tipo de job de geração de trilha.
 * Corresponde ao check constraint chk_jobs_type na tabela trail_generation_jobs.
 */
public enum JobType {
    /**
     * Geração completa de uma nova trilha
     */
    full_generation,

    /**
     * Preenchimento de lacunas em trilha existente
     */
    gap_fill,

    /**
     * Atualização/refresh de trilha existente
     */
    refresh
}
