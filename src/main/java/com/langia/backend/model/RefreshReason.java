package com.langia.backend.model;

/**
 * Motivos para regeneração de uma trilha.
 * Corresponde ao enum refresh_reason no banco de dados.
 */
public enum RefreshReason {
    /**
     * Nível CEFR do estudante mudou (subiu/desceu)
     */
    level_change,

    /**
     * Preferências de aprendizado foram alteradas
     */
    preferences_update,

    /**
     * Currículo base foi atualizado pela plataforma
     */
    curriculum_update,

    /**
     * Estudante solicitou nova trilha manualmente
     */
    manual_request
}
