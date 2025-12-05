package com.langia.backend.exception;

import java.util.UUID;

/**
 * Exceção lançada quando um documento não é encontrado.
 */
public class DocumentNotFoundException extends RuntimeException {

    public DocumentNotFoundException(UUID id) {
        super("Documento não encontrado: " + id);
    }

    public DocumentNotFoundException(String message) {
        super(message);
    }
}
