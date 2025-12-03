package com.langia.backend.dto.trail;

import com.langia.backend.model.RefreshReason;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO para requisição de refresh (regeneração) de trilha.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RefreshTrailRequestDTO {

    @NotNull(message = "Motivo do refresh é obrigatório")
    private RefreshReason reason;

    /**
     * Se true, mantém progresso de lições já completadas.
     */
    @Builder.Default
    private Boolean preserveProgress = true;

    /**
     * Novo código de nível (se reason = level_change).
     */
    private String newLevelCode;

    /**
     * Notas adicionais sobre o motivo do refresh.
     */
    private String notes;
}
