package com.langia.backend.exception;

/**
 * Exceção lançada quando a validação de senha falha.
 * Usada para erros de complexidade de senha (mínimo de caracteres, maiúsculas, etc).
 */
public class PasswordValidationException extends RuntimeException {

    private final String errors;

    public PasswordValidationException(String errors) {
        super(errors);
        this.errors = errors;
    }

    public String getErrors() {
        return errors;
    }
}
