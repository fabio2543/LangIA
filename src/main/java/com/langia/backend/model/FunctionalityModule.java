package com.langia.backend.model;

/**
 * Módulos/categorias de funcionalidades do sistema.
 */
public enum FunctionalityModule {
    OWN_PROFILE("Own Profile"),
    COURSES("Courses"),
    LESSONS("Lessons"),
    STUDENTS("Students"),
    TEACHERS("Teachers"),
    SYSTEM("System"),
    AI("AI"),
    WHATSAPP("WhatsApp");

    private final String displayName;

    FunctionalityModule(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }
}
