package com.langia.backend.model;

import java.util.UUID;

/**
 * Minimal contract representing a lesson that can be owned by a user. Domain
 * entities should implement this interface so that permission checks remain
 * decoupled from persistence details.
 */
public interface LessonContext {

    UUID getId();

    /**
     * @param userId teacher identifier
     * @return true when the provided user owns the class context
     */
    boolean isOwnedBy(UUID userId);
}
