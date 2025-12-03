package com.langia.backend.dto.trail;

import java.math.BigDecimal;
import java.util.UUID;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO para representar um descritor (Can-Do Statement).
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DescriptorDTO {

    private UUID id;
    private String code;
    private String description;
    private String descriptionEn;
    private String levelCode;
    private String competencyCode;
    private Integer orderIndex;
    private Boolean isCore;
    private BigDecimal estimatedHours;
}
