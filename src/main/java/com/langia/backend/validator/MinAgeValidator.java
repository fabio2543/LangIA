package com.langia.backend.validator;

import java.time.LocalDate;
import java.time.Period;

import com.langia.backend.annotation.MinAge;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

/**
 * Validador para a anotação @MinAge.
 * Verifica se a data de nascimento resulta em uma idade mínima.
 */
public class MinAgeValidator implements ConstraintValidator<MinAge, LocalDate> {

    private int minAge;

    @Override
    public void initialize(MinAge constraintAnnotation) {
        this.minAge = constraintAnnotation.value();
    }

    @Override
    public boolean isValid(LocalDate birthDate, ConstraintValidatorContext context) {
        // Null é válido - use @NotNull separadamente se necessário
        if (birthDate == null) {
            return true;
        }

        LocalDate today = LocalDate.now();

        // Data no futuro é inválida
        if (birthDate.isAfter(today)) {
            return false;
        }

        int age = Period.between(birthDate, today).getYears();
        return age >= minAge;
    }
}
