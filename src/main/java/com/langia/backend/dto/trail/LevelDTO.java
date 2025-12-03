package com.langia.backend.dto.trail;

import java.util.UUID;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO para representar um nível CEFR.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LevelDTO {

    private UUID id;
    private String code;
    private String name;
    private String description;
    private Integer orderIndex;
}
