package com.langia.backend.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO para requisição de recuperação de senha.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ForgotPasswordRequestDTO {

    @NotBlank(message = "Email is required")
    @Email(message = "Email must be valid")
    private String email;

    /**
     * Retorna o email sanitizado (trim + lowercase).
     */
    public String getEmail() {
        return email != null ? email.trim().toLowerCase() : null;
    }

    /**
     * Retorna o email bruto sem sanitização.
     */
    public String getRawEmail() {
        return email;
    }
}
