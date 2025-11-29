package com.langia.backend.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO for requesting email change.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class RequestEmailChangeDTO {

    @NotBlank(message = "Email is required")
    @Email(message = "Invalid email")
    @Size(max = 255)
    private String newEmail;
}
