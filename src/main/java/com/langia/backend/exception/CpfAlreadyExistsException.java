package com.langia.backend.exception;

/**
 * Exceção lançada quando o CPF informado já está cadastrado no sistema.
 */
public class CpfAlreadyExistsException extends RuntimeException {

    public CpfAlreadyExistsException() {
        super("CPF já cadastrado no sistema");
    }

    public CpfAlreadyExistsException(String message) {
        super(message);
    }
}
