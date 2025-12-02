package com.langia.backend.dto.student;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO para visualização de dados pessoais do estudante.
 * Usado na resposta do endpoint GET /api/v1/students/me/personal-data
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PersonalDataDTO {

    private UUID id;
    private String name;
    private String email;
    private String phone;
    private LocalDate birthDate;
    private String nativeLanguage;
    private String timezone;
    private String bio;
    private boolean emailVerified;
    private LocalDateTime emailVerifiedAt;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
