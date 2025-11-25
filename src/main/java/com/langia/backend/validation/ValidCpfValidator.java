package com.langia.backend.validation;

import org.springframework.beans.factory.annotation.Autowired;

import com.langia.backend.util.CpfValidator;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

/**
 * Implementação do validador para anotação @ValidCpf.
 * Utiliza CpfValidator para validar o CPF.
 */
public class ValidCpfValidator implements ConstraintValidator<ValidCpf, String> {

    @Autowired
    private CpfValidator cpfValidator;

    @Override
    public void initialize(ValidCpf constraintAnnotation) {
        // Não precisa de inicialização
    }

    @Override
    public boolean isValid(String cpf, ConstraintValidatorContext context) {
        // Permite valores nulos (use @NotBlank para obrigar preenchimento)
        if (cpf == null || cpf.isBlank()) {
            return true;
        }

        return cpfValidator.isValid(cpf);
    }
}
