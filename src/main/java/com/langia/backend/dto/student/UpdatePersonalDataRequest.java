package com.langia.backend.dto.student;

import java.time.LocalDate;

import com.langia.backend.annotation.MinAge;

import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO para atualização de dados pessoais do estudante.
 * Usado na requisição PATCH /api/v1/students/me/personal-data
 *
 * Todos os campos são opcionais - apenas os campos presentes serão atualizados.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UpdatePersonalDataRequest {

    /**
     * Nome do estudante.
     * Deve conter apenas letras e espaços, com mínimo de 3 caracteres.
     */
    @Size(min = 3, max = 100, message = "Nome deve ter entre 3 e 100 caracteres")
    @Pattern(regexp = "^[\\p{L}\\s]+$", message = "Nome deve conter apenas letras e espaços")
    private String name;

    /**
     * Data de nascimento do estudante.
     * Deve resultar em idade mínima de 13 anos.
     */
    @MinAge(value = 13, message = "Idade mínima: 13 anos")
    private LocalDate birthDate;

    /**
     * Idioma nativo do estudante.
     */
    @Size(max = 50, message = "Idioma nativo deve ter no máximo 50 caracteres")
    private String nativeLanguage;

    /**
     * Fuso horário do estudante (ex: "America/Sao_Paulo").
     */
    @Size(max = 50, message = "Timezone deve ter no máximo 50 caracteres")
    @Pattern(regexp = "^[A-Za-z_/]+$", message = "Timezone inválido")
    private String timezone;

    /**
     * Biografia do estudante.
     */
    @Size(max = 1000, message = "Bio deve ter no máximo 1000 caracteres")
    private String bio;
}
