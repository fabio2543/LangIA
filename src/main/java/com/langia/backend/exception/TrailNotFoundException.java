package com.langia.backend.exception;

import java.util.UUID;

/**
 * Exceção lançada quando uma trilha não é encontrada.
 */
public class TrailNotFoundException extends RuntimeException {

    public TrailNotFoundException(String message) {
        super(message);
    }

    public TrailNotFoundException(UUID trailId) {
        super("Trilha não encontrada: " + trailId);
    }

    public TrailNotFoundException(UUID studentId, String languageCode) {
        super("Trilha não encontrada para estudante " + studentId + " e idioma " + languageCode);
    }
}
