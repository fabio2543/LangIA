package com.langia.backend.dto;

import java.util.UUID;

import com.langia.backend.model.Functionality;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PermissionDTO {

    private UUID id;
    private String code;
    private String description;
    private Functionality.Module module;
    private boolean active;

    public static PermissionDTO fromEntity(Functionality functionality) {
        return PermissionDTO.builder()
                .id(functionality.getId())
                .code(functionality.getCode())
                .description(functionality.getDescription())
                .module(functionality.getModule())
                .active(functionality.isActive())
                .build();
    }
}

