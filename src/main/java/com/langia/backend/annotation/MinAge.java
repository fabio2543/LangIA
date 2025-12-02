package com.langia.backend.annotation;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import com.langia.backend.validator.MinAgeValidator;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;

/**
 * Validação customizada para idade mínima baseada em data de nascimento.
 *
 * <p>Exemplo de uso:</p>
 * <pre>
 * {@code
 * @MinAge(value = 13, message = "Idade mínima: 13 anos")
 * private LocalDate birthDate;
 * }
 * </pre>
 */
@Documented
@Constraint(validatedBy = MinAgeValidator.class)
@Target({ElementType.FIELD, ElementType.PARAMETER})
@Retention(RetentionPolicy.RUNTIME)
public @interface MinAge {

    /**
     * Idade mínima requerida em anos.
     */
    int value();

    /**
     * Mensagem de erro quando a validação falha.
     */
    String message() default "Idade mínima não atingida";

    /**
     * Grupos de validação.
     */
    Class<?>[] groups() default {};

    /**
     * Payload para metadados adicionais.
     */
    Class<? extends Payload>[] payload() default {};
}
