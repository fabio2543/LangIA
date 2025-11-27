package com.langia.backend.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.Set;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.langia.backend.config.AuthCookieProperties;
import com.langia.backend.config.TestSecurityConfig;
import com.langia.backend.dto.LoginRequestDTO;
import com.langia.backend.dto.LoginResponseDTO;
import com.langia.backend.dto.SessionData;
import com.langia.backend.exception.InvalidCredentialsException;
import com.langia.backend.model.UserProfile;
import com.langia.backend.service.AuthenticationService;
import com.langia.backend.util.TokenExtractor;

import jakarta.servlet.http.HttpServletRequest;

/**
 * Testes para o controller de autenticação.
 */
@WebMvcTest(controllers = AuthenticationController.class)
@Import(TestSecurityConfig.class)
class AuthenticationControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private AuthenticationService authenticationService;

    @MockBean
    private TokenExtractor tokenExtractor;

    @MockBean
    private AuthCookieProperties cookieProperties;

    private LoginRequestDTO validLoginRequest;
    private LoginResponseDTO loginResponse;
    private SessionData sessionData;
    private String validToken;

    @BeforeEach
    void setUp() {
        UUID userId = UUID.randomUUID();

        validLoginRequest = new LoginRequestDTO("test@example.com", "password123");

        Set<String> permissions = Set.of("view_courses", "view_lessons", "submit_exercises");

        loginResponse = LoginResponseDTO.builder()
                .token("eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.test")
                .userId(userId)
                .name("Test User")
                .email("test@example.com")
                .profile(UserProfile.STUDENT)
                .permissions(permissions)
                .expiresIn(3600000L)
                .build();

        sessionData = SessionData.builder()
                .userId(userId)
                .name("Test User")
                .email("test@example.com")
                .profile(UserProfile.STUDENT)
                .permissions(permissions)
                .createdAt(System.currentTimeMillis())
                .build();

        validToken = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.test";

        // Configura cookie properties para testes
        when(cookieProperties.getName()).thenReturn("langia_auth");
        when(cookieProperties.isSecure()).thenReturn(false);
        when(cookieProperties.getSameSite()).thenReturn("Lax");
    }

    // ========== Testes de Login ==========

    @Test
    void deveRealizarLoginComSucesso() throws Exception {
        // Arrange
        when(authenticationService.login(any(LoginRequestDTO.class))).thenReturn(loginResponse);

        // Act & Assert
        mockMvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(validLoginRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").value(loginResponse.getToken()))
                .andExpect(jsonPath("$.userId").value(loginResponse.getUserId().toString()))
                .andExpect(jsonPath("$.name").value(loginResponse.getName()))
                .andExpect(jsonPath("$.email").value(loginResponse.getEmail()))
                .andExpect(jsonPath("$.profile").value(loginResponse.getProfile().toString()))
                .andExpect(jsonPath("$.permissions").isArray())
                .andExpect(jsonPath("$.expiresIn").value(loginResponse.getExpiresIn()));
    }

    @Test
    void deveRetornar401ParaCredenciaisInvalidas() throws Exception {
        // Arrange
        when(authenticationService.login(any(LoginRequestDTO.class)))
                .thenThrow(new InvalidCredentialsException());

        // Act & Assert
        mockMvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(validLoginRequest)))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.message").value("Invalid credentials"));
    }

    @Test
    void deveRetornar400ParaEmailInvalido() throws Exception {
        // Arrange
        LoginRequestDTO invalidRequest = new LoginRequestDTO("email-invalido", "password123");

        // Act & Assert
        mockMvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(invalidRequest)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void deveRetornar400ParaEmailVazio() throws Exception {
        // Arrange
        LoginRequestDTO invalidRequest = new LoginRequestDTO("", "password123");

        // Act & Assert
        mockMvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(invalidRequest)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void deveRetornar400ParaSenhaVazia() throws Exception {
        // Arrange
        LoginRequestDTO invalidRequest = new LoginRequestDTO("test@example.com", "");

        // Act & Assert
        mockMvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(invalidRequest)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void deveRetornar500ParaErroInesperado() throws Exception {
        // Arrange
        when(authenticationService.login(any(LoginRequestDTO.class)))
                .thenThrow(new RuntimeException("Database error"));

        // Act & Assert
        mockMvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(validLoginRequest)))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.message").value("An unexpected error occurred"));
    }

    // ========== Testes de Logout ==========

    @Test
    void deveRealizarLogoutComSucesso() throws Exception {
        // Arrange
        when(tokenExtractor.extractFromRequest(any(HttpServletRequest.class))).thenReturn(validToken);
        when(authenticationService.logout(validToken)).thenReturn(true);

        // Act & Assert
        mockMvc.perform(post("/api/auth/logout")
                .header("Authorization", "Bearer " + validToken))
                .andExpect(status().isNoContent());
    }

    @Test
    void deveRetornar401ParaLogoutSemToken() throws Exception {
        // Arrange
        when(tokenExtractor.extractFromRequest(any(HttpServletRequest.class))).thenReturn(null);

        // Act & Assert
        mockMvc.perform(post("/api/auth/logout"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.message").value("No token provided"));
    }

    @Test
    void deveRetornar401ParaLogoutComTokenInvalido() throws Exception {
        // Arrange
        when(tokenExtractor.extractFromRequest(any(HttpServletRequest.class))).thenReturn(null);

        // Act & Assert
        mockMvc.perform(post("/api/auth/logout")
                .header("Authorization", "InvalidFormat"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.message").value("No token provided"));
    }

    @Test
    void deveRetornar401ParaSessaoJaExpirada() throws Exception {
        // Arrange
        when(tokenExtractor.extractFromRequest(any(HttpServletRequest.class))).thenReturn(validToken);
        when(authenticationService.logout(validToken)).thenReturn(false);

        // Act & Assert
        mockMvc.perform(post("/api/auth/logout")
                .header("Authorization", "Bearer " + validToken))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.message").value("Session not found or already expired"));
    }

    // ========== Testes de Validação ==========

    @Test
    void deveValidarSessaoComSucesso() throws Exception {
        // Arrange
        when(tokenExtractor.extractFromRequest(any(HttpServletRequest.class))).thenReturn(validToken);
        when(authenticationService.validateSession(validToken)).thenReturn(sessionData);

        // Act & Assert
        mockMvc.perform(get("/api/auth/validate")
                .header("Authorization", "Bearer " + validToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.valid").value(true))
                .andExpect(jsonPath("$.session.userId").value(sessionData.getUserId().toString()))
                .andExpect(jsonPath("$.session.email").value(sessionData.getEmail()))
                .andExpect(jsonPath("$.session.profile").value(sessionData.getProfile().toString()));
    }

    @Test
    void deveRetornar401ParaSessaoInvalida() throws Exception {
        // Arrange
        when(tokenExtractor.extractFromRequest(any(HttpServletRequest.class))).thenReturn(validToken);
        when(authenticationService.validateSession(validToken)).thenReturn(null);

        // Act & Assert
        mockMvc.perform(get("/api/auth/validate")
                .header("Authorization", "Bearer " + validToken))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.valid").value(false))
                .andExpect(jsonPath("$.session").isEmpty());
    }

    @Test
    void deveRetornar401ParaValidacaoSemToken() throws Exception {
        // Arrange
        when(tokenExtractor.extractFromRequest(any(HttpServletRequest.class))).thenReturn(null);

        // Act & Assert
        mockMvc.perform(get("/api/auth/validate"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.valid").value(false));
    }

    @Test
    void deveRetornar401ParaValidacaoComTokenMalFormado() throws Exception {
        // Arrange
        when(tokenExtractor.extractFromRequest(any(HttpServletRequest.class))).thenReturn(null);

        // Act & Assert
        mockMvc.perform(get("/api/auth/validate")
                .header("Authorization", "Token " + validToken))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.valid").value(false));
    }

    // ========== Testes de Renovação ==========

    @Test
    void deveRenovarSessaoComSucesso() throws Exception {
        // Arrange
        when(tokenExtractor.extractFromRequest(any(HttpServletRequest.class))).thenReturn(validToken);
        when(authenticationService.renewSession(validToken)).thenReturn(true);

        // Act & Assert
        mockMvc.perform(post("/api/auth/renew")
                .header("Authorization", "Bearer " + validToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Session renewed successfully"));
    }

    @Test
    void deveRetornar401ParaRenovacaoComTokenInvalido() throws Exception {
        // Arrange
        when(tokenExtractor.extractFromRequest(any(HttpServletRequest.class))).thenReturn(validToken);
        when(authenticationService.renewSession(validToken)).thenReturn(false);

        // Act & Assert
        mockMvc.perform(post("/api/auth/renew")
                .header("Authorization", "Bearer " + validToken))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.message").value("Invalid token or session not found"));
    }

    @Test
    void deveRetornar401ParaRenovacaoSemToken() throws Exception {
        // Arrange
        when(tokenExtractor.extractFromRequest(any(HttpServletRequest.class))).thenReturn(null);

        // Act & Assert
        mockMvc.perform(post("/api/auth/renew"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.message").value("No token provided"));
    }

    // ========== Testes de Formato de Token ==========

    @Test
    void deveExtrairTokenCorretamenteDoHeaderBearer() throws Exception {
        // Arrange
        when(tokenExtractor.extractFromRequest(any(HttpServletRequest.class))).thenReturn(validToken);
        when(authenticationService.validateSession(validToken)).thenReturn(sessionData);

        // Act & Assert
        mockMvc.perform(get("/api/auth/validate")
                .header("Authorization", "Bearer " + validToken))
                .andExpect(status().isOk());
    }

    @Test
    void deveRejeitarTokenSemPrefixoBearer() throws Exception {
        // Arrange
        when(tokenExtractor.extractFromRequest(any(HttpServletRequest.class))).thenReturn(null);

        // Act & Assert
        mockMvc.perform(get("/api/auth/validate")
                .header("Authorization", validToken))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void deveRejeitarHeaderAuthorizationVazio() throws Exception {
        // Arrange
        when(tokenExtractor.extractFromRequest(any(HttpServletRequest.class))).thenReturn(null);

        // Act & Assert
        mockMvc.perform(get("/api/auth/validate")
                .header("Authorization", ""))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void deveRejeitarHeaderAuthorizationApenasComBearer() throws Exception {
        // Arrange
        when(tokenExtractor.extractFromRequest(any(HttpServletRequest.class))).thenReturn(null);

        // Act & Assert
        mockMvc.perform(get("/api/auth/validate")
                .header("Authorization", "Bearer "))
                .andExpect(status().isUnauthorized());
    }
}
