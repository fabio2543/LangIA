package com.langia.backend.exception;

/**
 * Exceção lançada quando o telefone informado já está cadastrado no sistema.
 */
public class PhoneAlreadyExistsException extends RuntimeException {

    public PhoneAlreadyExistsException() {
        super("Telefone já cadastrado no sistema");
    }

    public PhoneAlreadyExistsException(String message) {
        super(message);
    }
}
