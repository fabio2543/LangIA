package com.langia.backend.exception;

import java.util.UUID;

/**
 * Exceção lançada quando uma lição não é encontrada.
 */
public class LessonNotFoundException extends RuntimeException {

    public LessonNotFoundException(String message) {
        super(message);
    }

    public LessonNotFoundException(UUID lessonId) {
        super("Lição não encontrada: " + lessonId);
    }
}
