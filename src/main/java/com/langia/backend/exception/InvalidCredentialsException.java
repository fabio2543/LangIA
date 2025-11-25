package com.langia.backend.exception;

/**
 * Exceção lançada quando credenciais de autenticação são inválidas.
 * Não especifica se o problema é email ou senha para evitar dar pistas a atacantes.
 */
public class InvalidCredentialsException extends RuntimeException {

    public InvalidCredentialsException() {
        super("Invalid credentials");
    }

    public InvalidCredentialsException(String message) {
        super(message);
    }
}
