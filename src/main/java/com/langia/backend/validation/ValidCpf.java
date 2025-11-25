package com.langia.backend.validation;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;

/**
 * Anotação para validação de CPF brasileiro.
 * Valida formato, dígitos verificadores e sequências inválidas.
 *
 * Uso:
 * <pre>
 * {@code
 * @ValidCpf
 * private String cpf;
 * }
 * </pre>
 */
@Documented
@Constraint(validatedBy = ValidCpfValidator.class)
@Target({ElementType.FIELD, ElementType.PARAMETER})
@Retention(RetentionPolicy.RUNTIME)
public @interface ValidCpf {

    String message() default "CPF is invalid";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};
}
