package com.langia.backend.dto;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Set;
import java.util.UUID;

import org.junit.jupiter.api.Test;

import com.langia.backend.model.UserProfile;

/**
 * Testes para o LoginResponseDTO.
 * Valida a estrutura e o builder do DTO de resposta.
 */
class LoginResponseDTOTest {

    @Test
    void deveCriarLoginResponseComBuilder() {
        // Given
        UUID userId = UUID.randomUUID();
        String token = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...";
        Set<String> permissions = Set.of("view_courses", "submit_exercises");

        // When
        LoginResponseDTO response = LoginResponseDTO.builder()
                .token(token)
                .userId(userId)
                .name("João Silva")
                .email("joao@example.com")
                .profile(UserProfile.STUDENT)
                .permissions(permissions)
                .expiresIn(3600000L)
                .build();

        // Then
        assertNotNull(response, "Response não deve ser nulo");
        assertEquals(token, response.getToken());
        assertEquals(userId, response.getUserId());
        assertEquals("João Silva", response.getName());
        assertEquals("joao@example.com", response.getEmail());
        assertEquals(UserProfile.STUDENT, response.getProfile());
        assertEquals(permissions, response.getPermissions());
        assertEquals(3600000L, response.getExpiresIn());
    }

    @Test
    void deveCriarLoginResponseComConstrutorVazio() {
        // When
        LoginResponseDTO response = new LoginResponseDTO();

        // Then
        assertNotNull(response, "Response não deve ser nulo");
    }

    @Test
    void deveCriarLoginResponseComConstrutorCompleto() {
        // Given
        UUID userId = UUID.randomUUID();
        String token = "token123";
        Set<String> permissions = Set.of("manage_users");

        // When
        LoginResponseDTO response = new LoginResponseDTO(
                token,
                userId,
                "Admin User",
                "admin@example.com",
                UserProfile.ADMIN,
                permissions,
                3600000L,
                true // onboardingCompleted
        );

        // Then
        assertEquals(token, response.getToken());
        assertEquals(userId, response.getUserId());
        assertEquals("Admin User", response.getName());
        assertEquals("admin@example.com", response.getEmail());
        assertEquals(UserProfile.ADMIN, response.getProfile());
        assertEquals(permissions, response.getPermissions());
        assertEquals(3600000L, response.getExpiresIn());
        assertEquals(true, response.getOnboardingCompleted());
    }

    @Test
    void devePermitirSettersParaTodosCampos() {
        // Given
        LoginResponseDTO response = new LoginResponseDTO();
        UUID userId = UUID.randomUUID();
        Set<String> permissions = Set.of("view_lessons", "create_courses");

        // When
        response.setToken("newToken");
        response.setUserId(userId);
        response.setName("Teacher Name");
        response.setEmail("teacher@example.com");
        response.setProfile(UserProfile.TEACHER);
        response.setPermissions(permissions);
        response.setExpiresIn(7200000L);

        // Then
        assertEquals("newToken", response.getToken());
        assertEquals(userId, response.getUserId());
        assertEquals("Teacher Name", response.getName());
        assertEquals("teacher@example.com", response.getEmail());
        assertEquals(UserProfile.TEACHER, response.getProfile());
        assertEquals(permissions, response.getPermissions());
        assertEquals(7200000L, response.getExpiresIn());
    }

    @Test
    void deveSuportarPermissoesVazias() {
        // Given
        Set<String> emptyPermissions = Set.of();

        // When
        LoginResponseDTO response = LoginResponseDTO.builder()
                .token("token")
                .userId(UUID.randomUUID())
                .name("User")
                .email("user@example.com")
                .profile(UserProfile.STUDENT)
                .permissions(emptyPermissions)
                .expiresIn(3600000L)
                .build();

        // Then
        assertNotNull(response.getPermissions());
        assertTrue(response.getPermissions().isEmpty());
    }

    @Test
    void deveSuportarMultiplasPermissoes() {
        // Given
        Set<String> multiplePermissions = Set.of(
                "view_courses",
                "create_courses",
                "edit_courses",
                "delete_courses",
                "manage_users"
        );

        // When
        LoginResponseDTO response = LoginResponseDTO.builder()
                .token("token")
                .userId(UUID.randomUUID())
                .name("Admin")
                .email("admin@example.com")
                .profile(UserProfile.ADMIN)
                .permissions(multiplePermissions)
                .expiresIn(3600000L)
                .build();

        // Then
        assertEquals(5, response.getPermissions().size());
        assertTrue(response.getPermissions().contains("view_courses"));
        assertTrue(response.getPermissions().contains("manage_users"));
    }

    @Test
    void deveValidarTodosOsPerfis() {
        // Test STUDENT
        LoginResponseDTO studentResponse = LoginResponseDTO.builder()
                .profile(UserProfile.STUDENT)
                .build();
        assertEquals(UserProfile.STUDENT, studentResponse.getProfile());

        // Test TEACHER
        LoginResponseDTO teacherResponse = LoginResponseDTO.builder()
                .profile(UserProfile.TEACHER)
                .build();
        assertEquals(UserProfile.TEACHER, teacherResponse.getProfile());

        // Test ADMIN
        LoginResponseDTO adminResponse = LoginResponseDTO.builder()
                .profile(UserProfile.ADMIN)
                .build();
        assertEquals(UserProfile.ADMIN, adminResponse.getProfile());
    }
}
