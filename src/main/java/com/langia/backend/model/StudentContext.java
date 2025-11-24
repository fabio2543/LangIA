package com.langia.backend.model;

import java.util.UUID;

/**
 * Minimal contract to describe student visibility rules. Domain entities that
 * represent a student should implement this interface.
 */
public interface StudentContext {

    UUID getId();

    /**
     * @param teacherId teacher identifier
     * @return true when the student belongs to a class taught by the teacher
     */
    boolean isStudentOfTeacher(UUID teacherId);
}
