package com.langia.backend.util;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Set;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.langia.backend.model.UserProfile;

/**
 * Testes para o PermissionMapper.
 * Valida o mapeamento correto de permissões para cada perfil.
 */
class PermissionMapperTest {

    private PermissionMapper permissionMapper;

    @BeforeEach
    void setUp() {
        permissionMapper = new PermissionMapper();
    }

    @Test
    void deveRetornarPermissoesParaStudent() {
        // When
        Set<String> permissions = permissionMapper.getPermissionsForProfile(UserProfile.STUDENT);

        // Then
        assertNotNull(permissions, "Permissões não devem ser nulas");
        assertFalse(permissions.isEmpty(), "Student deve ter permissões");
        assertTrue(permissions.contains("view_courses"), "Student deve poder ver cursos");
        assertTrue(permissions.contains("view_lessons"), "Student deve poder ver aulas");
        assertTrue(permissions.contains("submit_exercises"), "Student deve poder enviar exercícios");
        assertTrue(permissions.contains("view_progress"), "Student deve poder ver progresso");
        assertTrue(permissions.contains("chat_with_ai"), "Student deve poder usar chat com IA");
        assertTrue(permissions.contains("view_profile"), "Student deve poder ver perfil");
        assertTrue(permissions.contains("update_profile"), "Student deve poder atualizar perfil");
        assertEquals(7, permissions.size(), "Student deve ter exatamente 7 permissões");
    }

    @Test
    void deveRetornarPermissoesParaTeacher() {
        // When
        Set<String> permissions = permissionMapper.getPermissionsForProfile(UserProfile.TEACHER);

        // Then
        assertNotNull(permissions, "Permissões não devem ser nulas");
        assertFalse(permissions.isEmpty(), "Teacher deve ter permissões");
        assertTrue(permissions.contains("view_courses"), "Teacher deve poder ver cursos");
        assertTrue(permissions.contains("create_courses"), "Teacher deve poder criar cursos");
        assertTrue(permissions.contains("edit_courses"), "Teacher deve poder editar cursos");
        assertTrue(permissions.contains("delete_courses"), "Teacher deve poder deletar cursos");
        assertTrue(permissions.contains("view_students"), "Teacher deve poder ver estudantes");
        assertTrue(permissions.contains("view_student_progress"), "Teacher deve ver progresso de alunos");
        assertTrue(permissions.contains("grade_exercises"), "Teacher deve poder avaliar exercícios");
        assertTrue(permissions.contains("manage_class"), "Teacher deve poder gerenciar turmas");
        assertEquals(14, permissions.size(), "Teacher deve ter exatamente 14 permissões");
    }

    @Test
    void deveRetornarPermissoesParaAdmin() {
        // When
        Set<String> permissions = permissionMapper.getPermissionsForProfile(UserProfile.ADMIN);

        // Then
        assertNotNull(permissions, "Permissões não devem ser nulas");
        assertFalse(permissions.isEmpty(), "Admin deve ter permissões");
        assertTrue(permissions.contains("manage_users"), "Admin deve poder gerenciar usuários");
        assertTrue(permissions.contains("create_users"), "Admin deve poder criar usuários");
        assertTrue(permissions.contains("edit_users"), "Admin deve poder editar usuários");
        assertTrue(permissions.contains("delete_users"), "Admin deve poder deletar usuários");
        assertTrue(permissions.contains("view_system_stats"), "Admin deve ver estatísticas do sistema");
        assertTrue(permissions.contains("manage_settings"), "Admin deve gerenciar configurações");
        assertTrue(permissions.contains("manage_integrations"), "Admin deve gerenciar integrações");
        assertTrue(permissions.size() > 14, "Admin deve ter mais permissões que Teacher");
    }

    @Test
    void deveVerificarPermissaoEspecificaStudent() {
        // When & Then
        assertTrue(permissionMapper.hasPermission(UserProfile.STUDENT, "view_courses"));
        assertTrue(permissionMapper.hasPermission(UserProfile.STUDENT, "submit_exercises"));
        assertFalse(permissionMapper.hasPermission(UserProfile.STUDENT, "create_courses"));
        assertFalse(permissionMapper.hasPermission(UserProfile.STUDENT, "manage_users"));
    }

    @Test
    void deveVerificarPermissaoEspecificaTeacher() {
        // When & Then
        assertTrue(permissionMapper.hasPermission(UserProfile.TEACHER, "view_courses"));
        assertTrue(permissionMapper.hasPermission(UserProfile.TEACHER, "create_courses"));
        assertTrue(permissionMapper.hasPermission(UserProfile.TEACHER, "view_students"));
        assertFalse(permissionMapper.hasPermission(UserProfile.TEACHER, "manage_users"));
        assertFalse(permissionMapper.hasPermission(UserProfile.TEACHER, "delete_users"));
    }

    @Test
    void deveVerificarPermissaoEspecificaAdmin() {
        // When & Then
        assertTrue(permissionMapper.hasPermission(UserProfile.ADMIN, "manage_users"));
        assertTrue(permissionMapper.hasPermission(UserProfile.ADMIN, "create_users"));
        assertTrue(permissionMapper.hasPermission(UserProfile.ADMIN, "view_system_stats"));
        assertTrue(permissionMapper.hasPermission(UserProfile.ADMIN, "manage_settings"));
    }

    @Test
    void deveRetornarFalseParaPermissaoInexistente() {
        // When & Then
        assertFalse(permissionMapper.hasPermission(UserProfile.STUDENT, "permissao_inexistente"));
        assertFalse(permissionMapper.hasPermission(UserProfile.TEACHER, "permissao_invalida"));
        assertFalse(permissionMapper.hasPermission(UserProfile.ADMIN, "permissao_desconhecida"));
    }

    @Test
    void deveValidarHierarquiaDePermissoes() {
        // Given
        Set<String> studentPermissions = permissionMapper.getPermissionsForProfile(UserProfile.STUDENT);
        Set<String> teacherPermissions = permissionMapper.getPermissionsForProfile(UserProfile.TEACHER);
        Set<String> adminPermissions = permissionMapper.getPermissionsForProfile(UserProfile.ADMIN);

        // Then - Admin tem mais permissões que Teacher que tem mais que Student
        assertTrue(adminPermissions.size() > teacherPermissions.size(),
                "Admin deve ter mais permissões que Teacher");
        assertTrue(teacherPermissions.size() > studentPermissions.size(),
                "Teacher deve ter mais permissões que Student");
    }

    @Test
    void deveRetornarConjuntoImutavel() {
        // When
        Set<String> permissions = permissionMapper.getPermissionsForProfile(UserProfile.STUDENT);

        // Then - Tentar modificar deve lançar exceção
        try {
            permissions.add("nova_permissao");
            assertTrue(false, "Deve lançar UnsupportedOperationException");
        } catch (UnsupportedOperationException e) {
            // Esperado - conjunto é imutável
            assertTrue(true);
        }
    }

    @Test
    void deveValidarPermissoesComuns() {
        // Given
        Set<String> studentPermissions = permissionMapper.getPermissionsForProfile(UserProfile.STUDENT);
        Set<String> teacherPermissions = permissionMapper.getPermissionsForProfile(UserProfile.TEACHER);
        Set<String> adminPermissions = permissionMapper.getPermissionsForProfile(UserProfile.ADMIN);

        // Then - Todos devem poder ver perfil e cursos
        assertTrue(studentPermissions.contains("view_profile"));
        assertTrue(teacherPermissions.contains("view_profile"));
        assertTrue(adminPermissions.contains("view_profile"));

        assertTrue(studentPermissions.contains("view_courses"));
        assertTrue(teacherPermissions.contains("view_courses"));
        assertTrue(adminPermissions.contains("view_courses"));
    }

    @Test
    void deveValidarPermissoesExclusivasStudent() {
        // When
        Set<String> permissions = permissionMapper.getPermissionsForProfile(UserProfile.STUDENT);

        // Then
        assertTrue(permissions.contains("chat_with_ai"));
        assertFalse(permissionMapper.hasPermission(UserProfile.TEACHER, "submit_exercises"));
    }

    @Test
    void deveValidarPermissoesExclusivasAdmin() {
        // When & Then
        assertTrue(permissionMapper.hasPermission(UserProfile.ADMIN, "manage_users"));
        assertTrue(permissionMapper.hasPermission(UserProfile.ADMIN, "view_system_stats"));
        assertTrue(permissionMapper.hasPermission(UserProfile.ADMIN, "manage_integrations"));

        assertFalse(permissionMapper.hasPermission(UserProfile.TEACHER, "manage_users"));
        assertFalse(permissionMapper.hasPermission(UserProfile.STUDENT, "manage_users"));
    }
}
