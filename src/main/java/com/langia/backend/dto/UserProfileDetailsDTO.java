package com.langia.backend.dto;

import java.time.LocalDate;
import java.util.UUID;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO for returning user profile details (GET response).
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserProfileDetailsDTO {
    private UUID id;
    private String fullName;
    private String email;
    private String whatsappPhone;
    private String nativeLanguage;
    private String timezone;
    private LocalDate birthDate;
    private String bio;
}
