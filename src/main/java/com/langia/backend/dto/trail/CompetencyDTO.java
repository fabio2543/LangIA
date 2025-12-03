package com.langia.backend.dto.trail;

import java.util.UUID;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO para representar uma competência linguística.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CompetencyDTO {

    private UUID id;
    private String code;
    private String name;
    private String description;
    private String category;
    private String icon;
    private Integer orderIndex;
}
