package com.langia.backend.util;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import com.langia.backend.model.Profile;
import com.langia.backend.model.User;
import com.langia.backend.model.UserProfile;

/**
 * Testes para a classe JwtUtil.
 * Valida todas as operações de geração, validação e extração de informações de tokens JWT.
 */
@SpringBootTest
@ActiveProfiles("test")
class JwtUtilTest {

    @Autowired
    private JwtUtil jwtUtil;

    private User testUser;

    @BeforeEach
    void setUp() {
        Profile studentProfile = Profile.builder()
                .id(UUID.randomUUID())
                .code(UserProfile.STUDENT)
                .name("Student")
                .hierarchyLevel(1)
                .active(true)
                .build();

        testUser = User.builder()
                .id(UUID.randomUUID())
                .name("João Silva")
                .email("joao.silva@test.com")
                .cpfString("12345678900")
                .password("senhaEncriptada123")
                .profile(studentProfile)
                .phone("11987654321")
                .build();
    }

    @Test
    void deveGerarTokenComSucesso() {
        // When
        String token = jwtUtil.generateToken(testUser);

        // Then
        assertNotNull(token, "Token não deve ser nulo");
        assertFalse(token.isEmpty(), "Token não deve estar vazio");
        assertTrue(token.split("\\.").length == 3, "Token JWT deve ter 3 partes separadas por ponto");
    }

    @Test
    void deveExtrairEmailDoToken() {
        // Given
        String token = jwtUtil.generateToken(testUser);

        // When
        String email = jwtUtil.extractEmail(token);

        // Then
        assertEquals(testUser.getEmail(), email, "Email extraído deve ser igual ao email do usuário");
    }

    @Test
    void deveExtrairUserIdDoToken() {
        // Given
        String token = jwtUtil.generateToken(testUser);

        // When
        UUID userId = jwtUtil.extractUserId(token);

        // Then
        assertEquals(testUser.getId(), userId, "UserId extraído deve ser igual ao ID do usuário");
    }

    @Test
    void deveExtrairUserProfileDoToken() {
        // Given
        String token = jwtUtil.generateToken(testUser);

        // When
        UserProfile profile = jwtUtil.extractUserProfile(token);

        // Then
        assertEquals(testUser.getProfileCode(), profile, "Profile extraído deve ser igual ao profile do usuário");
    }

    @Test
    void deveExtrairUserNameDoToken() {
        // Given
        String token = jwtUtil.generateToken(testUser);

        // When
        String name = jwtUtil.extractUserName(token);

        // Then
        assertEquals(testUser.getName(), name, "Nome extraído deve ser igual ao nome do usuário");
    }

    @Test
    void deveValidarTokenValido() {
        // Given
        String token = jwtUtil.generateToken(testUser);

        // When
        Boolean isValid = jwtUtil.validateToken(token);

        // Then
        assertTrue(isValid, "Token válido deve retornar true");
    }

    @Test
    void deveValidarTokenComEmail() {
        // Given
        String token = jwtUtil.generateToken(testUser);

        // When
        Boolean isValid = jwtUtil.validateToken(token, testUser.getEmail());

        // Then
        assertTrue(isValid, "Token válido com email correto deve retornar true");
    }

    @Test
    void deveInvalidarTokenComEmailIncorreto() {
        // Given
        String token = jwtUtil.generateToken(testUser);

        // When
        Boolean isValid = jwtUtil.validateToken(token, "email.errado@test.com");

        // Then
        assertFalse(isValid, "Token com email incorreto deve retornar false");
    }

    @Test
    void deveVerificarQueTokenNaoEstaExpirado() {
        // Given
        String token = jwtUtil.generateToken(testUser);

        // When
        Boolean isExpired = jwtUtil.isTokenExpired(token);

        // Then
        assertFalse(isExpired, "Token recém criado não deve estar expirado");
    }

    @Test
    void deveExtrairDataDeExpiracao() {
        // Given
        String token = jwtUtil.generateToken(testUser);

        // When
        var expiration = jwtUtil.extractExpiration(token);

        // Then
        assertNotNull(expiration, "Data de expiração não deve ser nula");
        assertTrue(expiration.getTime() > System.currentTimeMillis(),
                "Data de expiração deve ser no futuro");
    }

    @Test
    void deveInvalidarTokenMalformado() {
        // Given
        String tokenMalformado = "token.invalido.aqui";

        // When
        Boolean isValid = jwtUtil.validateToken(tokenMalformado);

        // Then
        assertFalse(isValid, "Token malformado deve retornar false");
    }

    @Test
    void deveInvalidarTokenVazio() {
        // Given
        String tokenVazio = "";

        // When
        Boolean isValid = jwtUtil.validateToken(tokenVazio);

        // Then
        assertFalse(isValid, "Token vazio deve retornar false");
    }
}
