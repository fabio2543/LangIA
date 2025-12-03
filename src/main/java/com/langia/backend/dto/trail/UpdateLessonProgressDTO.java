package com.langia.backend.dto.trail;

import java.math.BigDecimal;

import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO para atualização de progresso de uma lição.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UpdateLessonProgressDTO {

    /**
     * Se true, marca a lição como completada.
     */
    @Builder.Default
    private Boolean completed = false;

    /**
     * Score obtido na lição (0.00 a 100.00).
     */
    @DecimalMin(value = "0.00", message = "Score mínimo é 0")
    @DecimalMax(value = "100.00", message = "Score máximo é 100")
    private BigDecimal score;

    /**
     * Tempo gasto na lição em segundos.
     */
    @Min(value = 0, message = "Tempo gasto não pode ser negativo")
    private Integer timeSpentSeconds;

    /**
     * Respostas do usuário (para exercícios).
     */
    private Object userResponses;
}
