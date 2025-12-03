package com.langia.backend.model;

/**
 * Status de um job de geração de trilha.
 * Corresponde ao enum generation_job_status no banco de dados.
 */
public enum GenerationJobStatus {
    /**
     * Job na fila aguardando processamento
     */
    QUEUED,

    /**
     * Job sendo processado (IA gerando conteúdo)
     */
    PROCESSING,

    /**
     * Job finalizado com sucesso
     */
    COMPLETED,

    /**
     * Job falhou (erro na geração, timeout, etc.)
     */
    FAILED,

    /**
     * Job cancelado pelo usuário ou sistema
     */
    CANCELLED
}
