package com.langia.backend.exception;

import java.util.UUID;

/**
 * Exceção lançada quando ocorre erro na geração de trilha.
 */
public class TrailGenerationException extends RuntimeException {

    private final UUID trailId;
    private final String step;

    public TrailGenerationException(String message) {
        super(message);
        this.trailId = null;
        this.step = null;
    }

    public TrailGenerationException(String message, Throwable cause) {
        super(message, cause);
        this.trailId = null;
        this.step = null;
    }

    public TrailGenerationException(UUID trailId, String step, String message) {
        super("Erro na geração da trilha " + trailId + " no passo '" + step + "': " + message);
        this.trailId = trailId;
        this.step = step;
    }

    public TrailGenerationException(UUID trailId, String step, String message, Throwable cause) {
        super("Erro na geração da trilha " + trailId + " no passo '" + step + "': " + message, cause);
        this.trailId = trailId;
        this.step = step;
    }

    public UUID getTrailId() {
        return trailId;
    }

    public String getStep() {
        return step;
    }
}
