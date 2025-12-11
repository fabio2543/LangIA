package com.langia.backend.dto;

import java.util.UUID;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class SrsReviewRequest {
    @NotNull(message = "Card ID é obrigatório")
    private UUID cardId;

    @NotNull(message = "Quality é obrigatório")
    @Min(value = 0, message = "Quality deve ser entre 0 e 5")
    @Max(value = 5, message = "Quality deve ser entre 0 e 5")
    private Integer quality;
}
