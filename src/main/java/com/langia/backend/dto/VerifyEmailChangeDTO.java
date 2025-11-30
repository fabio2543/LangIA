package com.langia.backend.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO for verifying email change with code.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class VerifyEmailChangeDTO {

    @NotBlank(message = "Code is required")
    @Size(min = 6, max = 6, message = "Code must have 6 digits")
    @Pattern(regexp = "^\\d{6}$", message = "Code must contain only numbers")
    private String code;
}
