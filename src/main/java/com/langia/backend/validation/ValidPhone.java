package com.langia.backend.validation;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;

/**
 * Anotação para validação de telefone brasileiro (fixo ou celular).
 * Valida DDD, formato de celular (9 dígitos) e fixo (8 dígitos).
 *
 * Uso:
 * <pre>
 * {@code
 * @ValidPhone
 * private String phone;
 * }
 * </pre>
 */
@Documented
@Constraint(validatedBy = ValidPhoneValidator.class)
@Target({ElementType.FIELD, ElementType.PARAMETER})
@Retention(RetentionPolicy.RUNTIME)
public @interface ValidPhone {

    String message() default "Phone number is invalid";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};
}
