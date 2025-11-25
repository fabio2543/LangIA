package com.langia.backend.util;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.springframework.stereotype.Component;

import com.langia.backend.model.UserProfile;

/**
 * Componente responsável por mapear permissões de cada perfil de usuário.
 * Centraliza as regras de autorização do sistema.
 */
@Component
public class PermissionMapper {

    private static final Map<UserProfile, Set<String>> PROFILE_PERMISSIONS;

    static {
        Map<UserProfile, Set<String>> permissions = new HashMap<>();

        // Permissões do STUDENT
        permissions.put(UserProfile.STUDENT, Set.of(
                "view_courses",
                "view_lessons",
                "submit_exercises",
                "view_progress",
                "chat_with_ai",
                "view_profile",
                "update_profile"
        ));

        // Permissões do TEACHER
        permissions.put(UserProfile.TEACHER, Set.of(
                "view_courses",
                "create_courses",
                "edit_courses",
                "delete_courses",
                "view_lessons",
                "create_lessons",
                "edit_lessons",
                "delete_lessons",
                "view_students",
                "view_student_progress",
                "grade_exercises",
                "view_profile",
                "update_profile",
                "manage_class"
        ));

        // Permissões do ADMIN
        permissions.put(UserProfile.ADMIN, Set.of(
                "view_courses",
                "create_courses",
                "edit_courses",
                "delete_courses",
                "view_lessons",
                "create_lessons",
                "edit_lessons",
                "delete_lessons",
                "view_students",
                "view_teachers",
                "view_student_progress",
                "grade_exercises",
                "view_profile",
                "update_profile",
                "manage_class",
                "manage_users",
                "create_users",
                "edit_users",
                "delete_users",
                "view_system_stats",
                "manage_settings",
                "manage_integrations"
        ));

        PROFILE_PERMISSIONS = Collections.unmodifiableMap(permissions);
    }

    /**
     * Obtém o conjunto de permissões para um perfil específico.
     *
     * @param profile perfil do usuário
     * @return conjunto imutável de permissões
     */
    public Set<String> getPermissionsForProfile(UserProfile profile) {
        return PROFILE_PERMISSIONS.getOrDefault(profile, Collections.emptySet());
    }

    /**
     * Verifica se um perfil possui uma permissão específica.
     *
     * @param profile perfil do usuário
     * @param permission permissão a ser verificada
     * @return true se o perfil possui a permissão, false caso contrário
     */
    public boolean hasPermission(UserProfile profile, String permission) {
        Set<String> permissions = PROFILE_PERMISSIONS.get(profile);
        return permissions != null && permissions.contains(permission);
    }
}
