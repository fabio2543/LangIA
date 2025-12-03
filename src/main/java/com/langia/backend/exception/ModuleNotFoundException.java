package com.langia.backend.exception;

import java.util.UUID;

/**
 * Exceção lançada quando um módulo não é encontrado.
 */
public class ModuleNotFoundException extends RuntimeException {

    public ModuleNotFoundException(String message) {
        super(message);
    }

    public ModuleNotFoundException(UUID moduleId) {
        super("Módulo não encontrado: " + moduleId);
    }
}
