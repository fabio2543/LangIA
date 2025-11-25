package com.langia.backend.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Set;
import java.util.UUID;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.test.context.ActiveProfiles;

import com.langia.backend.dto.SessionData;
import com.langia.backend.model.UserProfile;

/**
 * Testes para o SessionService.
 * Valida todas as operações de gerenciamento de sessões no Redis.
 */
@SpringBootTest
@ActiveProfiles("test")
class SessionServiceTest {

    @Autowired
    private SessionService sessionService;

    @Autowired
    private RedisTemplate<String, SessionData> sessionRedisTemplate;

    private SessionData testSessionData;
    private String testToken;

    @BeforeEach
    void setUp() {
        testToken = "test-jwt-token-" + UUID.randomUUID();

        testSessionData = SessionData.builder()
                .userId(UUID.randomUUID())
                .name("Test User")
                .email("test@example.com")
                .profile(UserProfile.STUDENT)
                .permissions(Set.of("view_courses", "submit_exercises"))
                .build();
    }

    @AfterEach
    void tearDown() {
        // Limpa a sessão de teste
        if (testToken != null) {
            sessionService.removeSession(testToken);
        }
    }

    @Test
    void deveSalvarSessaoComSucesso() {
        // When
        sessionService.saveSession(testToken, testSessionData);

        // Then
        SessionData retrieved = sessionService.getSession(testToken);
        assertNotNull(retrieved, "Sessão deveria existir no Redis");
        assertEquals(testSessionData.getUserId(), retrieved.getUserId());
        assertEquals(testSessionData.getName(), retrieved.getName());
        assertEquals(testSessionData.getEmail(), retrieved.getEmail());
        assertEquals(testSessionData.getProfile(), retrieved.getProfile());
        assertNotNull(retrieved.getCreatedAt(), "CreatedAt deveria ser preenchido");
    }

    @Test
    void deveRecuperarSessaoExistente() {
        // Given
        sessionService.saveSession(testToken, testSessionData);

        // When
        SessionData retrieved = sessionService.getSession(testToken);

        // Then
        assertNotNull(retrieved);
        assertEquals(testSessionData.getEmail(), retrieved.getEmail());
    }

    @Test
    void deveRetornarNullParaSessaoInexistente() {
        // When
        SessionData retrieved = sessionService.getSession("token-inexistente");

        // Then
        assertNull(retrieved, "Deveria retornar null para token inexistente");
    }

    @Test
    void deveRemoverSessaoComSucesso() {
        // Given
        sessionService.saveSession(testToken, testSessionData);
        assertTrue(sessionService.sessionExists(testToken));

        // When
        boolean removed = sessionService.removeSession(testToken);

        // Then
        assertTrue(removed, "Deveria retornar true ao remover sessão existente");
        assertFalse(sessionService.sessionExists(testToken), "Sessão não deveria mais existir");
    }

    @Test
    void deveRetornarFalseAoRemoverSessaoInexistente() {
        // When
        boolean removed = sessionService.removeSession("token-inexistente");

        // Then
        assertFalse(removed, "Deveria retornar false ao tentar remover sessão inexistente");
    }

    @Test
    void deveVerificarExistenciaDeSessao() {
        // Given
        sessionService.saveSession(testToken, testSessionData);

        // When & Then
        assertTrue(sessionService.sessionExists(testToken), "Sessão deveria existir");
        assertFalse(sessionService.sessionExists("token-inexistente"), "Sessão inexistente não deveria existir");
    }

    @Test
    void deveRenovarSessaoExistente() {
        // Given
        sessionService.saveSession(testToken, testSessionData);
        long ttlInicial = sessionService.getSessionTTL(testToken);

        // When
        boolean renewed = sessionService.renewSession(testToken);

        // Then
        assertTrue(renewed, "Deveria renovar sessão existente");
        long ttlDepois = sessionService.getSessionTTL(testToken);
        assertTrue(ttlDepois > 0, "TTL deveria ser positivo após renovação");
    }

    @Test
    void deveRetornarFalseAoRenovarSessaoInexistente() {
        // When
        boolean renewed = sessionService.renewSession("token-inexistente");

        // Then
        assertFalse(renewed, "Não deveria renovar sessão inexistente");
    }

    @Test
    void deveObterTTLDaSessao() {
        // Given
        sessionService.saveSession(testToken, testSessionData);

        // When
        long ttl = sessionService.getSessionTTL(testToken);

        // Then
        assertTrue(ttl > 0, "TTL deveria ser positivo");
        assertTrue(ttl <= 3600, "TTL deveria ser no máximo 1 hora (3600 segundos)");
    }

    @Test
    void deveRetornarValorNegativoParaTTLDeSessaoInexistente() {
        // When
        long ttl = sessionService.getSessionTTL("token-inexistente");

        // Then
        assertTrue(ttl < 0, "Deveria retornar valor negativo para sessão inexistente");
    }

    @Test
    void deveSalvarPermissoesCorretamente() {
        // Given
        Set<String> permissions = Set.of("view_courses", "create_courses", "manage_users");
        testSessionData.setPermissions(permissions);

        // When
        sessionService.saveSession(testToken, testSessionData);
        SessionData retrieved = sessionService.getSession(testToken);

        // Then
        assertNotNull(retrieved.getPermissions());
        assertEquals(3, retrieved.getPermissions().size());
        assertTrue(retrieved.getPermissions().contains("view_courses"));
        assertTrue(retrieved.getPermissions().contains("create_courses"));
        assertTrue(retrieved.getPermissions().contains("manage_users"));
    }

    @Test
    void deveSalvarDiferentesPerfisCorretamente() {
        // Test STUDENT
        testSessionData.setProfile(UserProfile.STUDENT);
        sessionService.saveSession(testToken + "-student", testSessionData);
        assertEquals(UserProfile.STUDENT,
                sessionService.getSession(testToken + "-student").getProfile());

        // Test TEACHER
        testSessionData.setProfile(UserProfile.TEACHER);
        sessionService.saveSession(testToken + "-teacher", testSessionData);
        assertEquals(UserProfile.TEACHER,
                sessionService.getSession(testToken + "-teacher").getProfile());

        // Test ADMIN
        testSessionData.setProfile(UserProfile.ADMIN);
        sessionService.saveSession(testToken + "-admin", testSessionData);
        assertEquals(UserProfile.ADMIN,
                sessionService.getSession(testToken + "-admin").getProfile());

        // Cleanup
        sessionService.removeSession(testToken + "-student");
        sessionService.removeSession(testToken + "-teacher");
        sessionService.removeSession(testToken + "-admin");
    }

    @Test
    void devePreencherTimestampDeCreatedAt() {
        // Given
        long antes = System.currentTimeMillis();

        // When
        sessionService.saveSession(testToken, testSessionData);
        SessionData retrieved = sessionService.getSession(testToken);

        // Then
        long depois = System.currentTimeMillis();
        assertNotNull(retrieved.getCreatedAt());
        assertTrue(retrieved.getCreatedAt() >= antes);
        assertTrue(retrieved.getCreatedAt() <= depois);
    }

    @Test
    void devePermitirMultiplasSessoesSimultaneas() {
        // Given
        String token1 = "token-1-" + UUID.randomUUID();
        String token2 = "token-2-" + UUID.randomUUID();

        SessionData session1 = SessionData.builder()
                .userId(UUID.randomUUID())
                .name("User 1")
                .email("user1@example.com")
                .profile(UserProfile.STUDENT)
                .permissions(Set.of("view_courses"))
                .build();

        SessionData session2 = SessionData.builder()
                .userId(UUID.randomUUID())
                .name("User 2")
                .email("user2@example.com")
                .profile(UserProfile.TEACHER)
                .permissions(Set.of("create_courses"))
                .build();

        // When
        sessionService.saveSession(token1, session1);
        sessionService.saveSession(token2, session2);

        // Then
        SessionData retrieved1 = sessionService.getSession(token1);
        SessionData retrieved2 = sessionService.getSession(token2);

        assertNotNull(retrieved1);
        assertNotNull(retrieved2);
        assertEquals("user1@example.com", retrieved1.getEmail());
        assertEquals("user2@example.com", retrieved2.getEmail());
        assertEquals(UserProfile.STUDENT, retrieved1.getProfile());
        assertEquals(UserProfile.TEACHER, retrieved2.getProfile());

        // Cleanup
        sessionService.removeSession(token1);
        sessionService.removeSession(token2);
    }

    @Test
    void deveRemoverTodasAsSessoesDoUsuario() {
        // Given
        UUID userId = UUID.randomUUID();
        String token1 = "token-user-1-" + UUID.randomUUID();
        String token2 = "token-user-2-" + UUID.randomUUID();

        SessionData session1 = SessionData.builder()
                .userId(userId)
                .name("User")
                .email("user@example.com")
                .profile(UserProfile.STUDENT)
                .permissions(Set.of("view_courses"))
                .build();

        SessionData session2 = SessionData.builder()
                .userId(userId)
                .name("User")
                .email("user@example.com")
                .profile(UserProfile.STUDENT)
                .permissions(Set.of("view_courses"))
                .build();

        sessionService.saveSession(token1, session1);
        sessionService.saveSession(token2, session2);

        // When
        long removedCount = sessionService.removeAllUserSessions(userId.toString());

        // Then
        assertTrue(removedCount >= 2, "Deveria ter removido pelo menos 2 sessões");
        assertFalse(sessionService.sessionExists(token1));
        assertFalse(sessionService.sessionExists(token2));
    }
}
