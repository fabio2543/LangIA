package com.langia.backend.validation;

import org.springframework.beans.factory.annotation.Autowired;

import com.langia.backend.util.PhoneValidator;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

/**
 * Implementação do validador para anotação @ValidPhone.
 * Utiliza PhoneValidator para validar telefones brasileiros.
 */
public class ValidPhoneValidator implements ConstraintValidator<ValidPhone, String> {

    @Autowired
    private PhoneValidator phoneValidator;

    @Override
    public void initialize(ValidPhone constraintAnnotation) {
        // Não precisa de inicialização
    }

    @Override
    public boolean isValid(String phone, ConstraintValidatorContext context) {
        // Permite valores nulos (use @NotBlank para obrigar preenchimento)
        if (phone == null || phone.isBlank()) {
            return true;
        }

        return phoneValidator.isValid(phone);
    }
}
