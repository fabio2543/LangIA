package com.langia.backend.dto;

import java.time.LocalDate;

import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO for updating personal data (PATCH request).
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class UpdatePersonalDataDTO {

    @Size(min = 3, max = 150, message = "Name must be between 3 and 150 characters")
    @Pattern(regexp = "^[a-zA-ZÀ-ÿ\\s]+$", message = "Name must contain only letters")
    private String fullName;

    @Pattern(regexp = "^\\+[1-9]\\d{1,14}$", message = "Phone must be in E.164 format")
    private String whatsappPhone;

    private String nativeLanguage;

    @Pattern(regexp = "^[A-Za-z]+/[A-Za-z_]+$", message = "Invalid timezone format")
    private String timezone;

    private LocalDate birthDate;

    @Size(max = 500, message = "Bio must be at most 500 characters")
    private String bio;

    @AssertTrue(message = "Minimum age is 13 years")
    public boolean isAgeValid() {
        if (birthDate == null) {
            return true;
        }
        return birthDate.plusYears(13).isBefore(LocalDate.now())
                || birthDate.plusYears(13).isEqual(LocalDate.now());
    }
}
