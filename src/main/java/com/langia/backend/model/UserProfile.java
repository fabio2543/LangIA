package com.langia.backend.model;

public enum UserProfile {
    STUDENT(1),
    TEACHER(2),
    ADMIN(3);

    private final int hierarchyLevel;

    UserProfile(int hierarchyLevel) {
        this.hierarchyLevel = hierarchyLevel;
    }

    public int getHierarchyLevel() {
        return hierarchyLevel;
    }
}
