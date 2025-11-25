package com.langia.backend.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.util.ReflectionTestUtils;

import com.langia.backend.dto.LoginRequestDTO;
import com.langia.backend.dto.LoginResponseDTO;
import com.langia.backend.dto.SessionData;
import com.langia.backend.model.User;
import com.langia.backend.model.UserProfile;
import com.langia.backend.repository.UserRepository;
import com.langia.backend.util.JwtUtil;
import com.langia.backend.util.PermissionMapper;

/**
 * Testes para o serviço de autenticação.
 */
@ExtendWith(MockitoExtension.class)
class AuthenticationServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private JwtUtil jwtUtil;

    @Mock
    private SessionService sessionService;

    @Mock
    private PermissionMapper permissionMapper;

    @InjectMocks
    private AuthenticationService authenticationService;

    private User testUser;
    private LoginRequestDTO loginRequest;
    private String testToken;
    private Set<String> testPermissions;
    private SessionData testSessionData;

    @BeforeEach
    void setUp() {
        // Configuração do usuário de teste
        testUser = User.builder()
                .id(UUID.randomUUID())
                .name("Test User")
                .email("test@example.com")
                .password("$2a$12$hashedPassword")
                .profile(UserProfile.STUDENT)
                .cpfString("11144477735")
                .phone("11987654321")
                .build();

        // Configuração do request de login
        loginRequest = new LoginRequestDTO("test@example.com", "plainPassword");

        // Token de teste
        testToken = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.test";

        // Permissões de teste
        testPermissions = Set.of("view_courses", "view_lessons", "submit_exercises");

        // Session data de teste
        testSessionData = SessionData.builder()
                .userId(testUser.getId())
                .name(testUser.getName())
                .email(testUser.getEmail())
                .profile(testUser.getProfile())
                .permissions(testPermissions)
                .createdAt(System.currentTimeMillis())
                .build();

        // Configura expiração JWT via reflection
        ReflectionTestUtils.setField(authenticationService, "jwtExpiration", 3600000L);
    }

    // ========== Testes de Login ==========

    @Test
    void deveRealizarLoginComSucesso() {
        // Arrange
        when(userRepository.findByEmail(loginRequest.getEmail())).thenReturn(Optional.of(testUser));
        when(passwordEncoder.matches(loginRequest.getPassword(), testUser.getPassword())).thenReturn(true);
        when(jwtUtil.generateToken(testUser)).thenReturn(testToken);
        when(permissionMapper.getPermissionsForProfile(testUser.getProfile())).thenReturn(testPermissions);

        // Act
        LoginResponseDTO response = authenticationService.login(loginRequest);

        // Assert
        assertNotNull(response);
        assertEquals(testToken, response.getToken());
        assertEquals(testUser.getId(), response.getUserId());
        assertEquals(testUser.getName(), response.getName());
        assertEquals(testUser.getEmail(), response.getEmail());
        assertEquals(testUser.getProfile(), response.getProfile());
        assertEquals(testPermissions, response.getPermissions());
        assertEquals(3600000L, response.getExpiresIn());

        // Verifica que a sessão foi salva
        verify(sessionService).saveSession(anyString(), any(SessionData.class));
    }

    @Test
    void deveRejeitarLoginComEmailInexistente() {
        // Arrange
        when(userRepository.findByEmail(loginRequest.getEmail())).thenReturn(Optional.empty());

        // Act & Assert
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            authenticationService.login(loginRequest);
        });

        assertEquals("Invalid credentials", exception.getMessage());

        // Verifica que a senha não foi verificada
        verify(passwordEncoder, never()).matches(anyString(), anyString());
        // Verifica que nenhuma sessão foi criada
        verify(sessionService, never()).saveSession(anyString(), any(SessionData.class));
    }

    @Test
    void deveRejeitarLoginComSenhaIncorreta() {
        // Arrange
        when(userRepository.findByEmail(loginRequest.getEmail())).thenReturn(Optional.of(testUser));
        when(passwordEncoder.matches(loginRequest.getPassword(), testUser.getPassword())).thenReturn(false);

        // Act & Assert
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            authenticationService.login(loginRequest);
        });

        assertEquals("Invalid credentials", exception.getMessage());

        // Verifica que nenhum token foi gerado
        verify(jwtUtil, never()).generateToken(any(User.class));
        // Verifica que nenhuma sessão foi criada
        verify(sessionService, never()).saveSession(anyString(), any(SessionData.class));
    }

    @Test
    void deveIncluirPermissoesCorretas() {
        // Arrange
        when(userRepository.findByEmail(loginRequest.getEmail())).thenReturn(Optional.of(testUser));
        when(passwordEncoder.matches(loginRequest.getPassword(), testUser.getPassword())).thenReturn(true);
        when(jwtUtil.generateToken(testUser)).thenReturn(testToken);
        when(permissionMapper.getPermissionsForProfile(testUser.getProfile())).thenReturn(testPermissions);

        // Act
        LoginResponseDTO response = authenticationService.login(loginRequest);

        // Assert
        assertNotNull(response.getPermissions());
        assertEquals(3, response.getPermissions().size());
        assertTrue(response.getPermissions().contains("view_courses"));
        assertTrue(response.getPermissions().contains("view_lessons"));
        assertTrue(response.getPermissions().contains("submit_exercises"));

        // Verifica que as permissões foram buscadas
        verify(permissionMapper).getPermissionsForProfile(testUser.getProfile());
    }

    // ========== Testes de Validação de Sessão ==========

    @Test
    void deveValidarSessaoComSucesso() {
        // Arrange
        when(jwtUtil.validateToken(testToken)).thenReturn(true);
        when(sessionService.getSession(testToken)).thenReturn(testSessionData);

        // Act
        SessionData result = authenticationService.validateSession(testToken);

        // Assert
        assertNotNull(result);
        assertEquals(testSessionData.getUserId(), result.getUserId());
        assertEquals(testSessionData.getEmail(), result.getEmail());
        assertEquals(testSessionData.getProfile(), result.getProfile());

        // Verifica que o token foi validado e a sessão buscada
        verify(jwtUtil).validateToken(testToken);
        verify(sessionService).getSession(testToken);
    }

    @Test
    void deveRetornarNullParaTokenJWTInvalido() {
        // Arrange
        when(jwtUtil.validateToken(testToken)).thenReturn(false);

        // Act
        SessionData result = authenticationService.validateSession(testToken);

        // Assert
        assertNull(result);

        // Verifica que a sessão não foi buscada no Redis
        verify(sessionService, never()).getSession(anyString());
    }

    @Test
    void deveRetornarNullParaSessaoNaoEncontradaNoRedis() {
        // Arrange
        when(jwtUtil.validateToken(testToken)).thenReturn(true);
        when(sessionService.getSession(testToken)).thenReturn(null);

        // Act
        SessionData result = authenticationService.validateSession(testToken);

        // Assert
        assertNull(result);

        // Verifica que tentou buscar a sessão
        verify(sessionService).getSession(testToken);
    }

    @Test
    void deveRetornarTrueParaSessaoValida() {
        // Arrange
        when(jwtUtil.validateToken(testToken)).thenReturn(true);
        when(sessionService.getSession(testToken)).thenReturn(testSessionData);

        // Act
        boolean isValid = authenticationService.isSessionValid(testToken);

        // Assert
        assertTrue(isValid);
    }

    @Test
    void deveRetornarFalseParaSessaoInvalida() {
        // Arrange
        when(jwtUtil.validateToken(testToken)).thenReturn(false);

        // Act
        boolean isValid = authenticationService.isSessionValid(testToken);

        // Assert
        assertFalse(isValid);
    }

    // ========== Testes de Logout ==========

    @Test
    void deveRealizarLogoutComSucesso() {
        // Arrange
        when(sessionService.removeSession(testToken)).thenReturn(true);

        // Act
        boolean result = authenticationService.logout(testToken);

        // Assert
        assertTrue(result);

        // Verifica que a sessão foi removida
        verify(sessionService).removeSession(testToken);
    }

    @Test
    void deveRetornarFalseAoRemoverSessaoInexistente() {
        // Arrange
        when(sessionService.removeSession(testToken)).thenReturn(false);

        // Act
        boolean result = authenticationService.logout(testToken);

        // Assert
        assertFalse(result);

        // Verifica que tentou remover a sessão
        verify(sessionService).removeSession(testToken);
    }

    // ========== Testes de Renovação de Sessão ==========

    @Test
    void deveRenovarSessaoComSucesso() {
        // Arrange
        when(jwtUtil.validateToken(testToken)).thenReturn(true);
        when(sessionService.renewSession(testToken)).thenReturn(true);

        // Act
        boolean result = authenticationService.renewSession(testToken);

        // Assert
        assertTrue(result);

        // Verifica que o token foi validado e a sessão renovada
        verify(jwtUtil).validateToken(testToken);
        verify(sessionService).renewSession(testToken);
    }

    @Test
    void naoDeveRenovarSessaoComTokenInvalido() {
        // Arrange
        when(jwtUtil.validateToken(testToken)).thenReturn(false);

        // Act
        boolean result = authenticationService.renewSession(testToken);

        // Assert
        assertFalse(result);

        // Verifica que a sessão não foi renovada
        verify(sessionService, never()).renewSession(anyString());
    }

    @Test
    void deveRetornarFalseAoRenovarSessaoInexistente() {
        // Arrange
        when(jwtUtil.validateToken(testToken)).thenReturn(true);
        when(sessionService.renewSession(testToken)).thenReturn(false);

        // Act
        boolean result = authenticationService.renewSession(testToken);

        // Assert
        assertFalse(result);

        // Verifica que tentou renovar
        verify(sessionService).renewSession(testToken);
    }

    // ========== Testes de Diferentes Perfis ==========

    @Test
    void deveRealizarLoginComPerfilTeacher() {
        // Arrange
        testUser.setProfile(UserProfile.TEACHER);
        Set<String> teacherPermissions = Set.of(
                "view_courses", "create_courses", "edit_courses",
                "view_students", "grade_exercises"
        );

        when(userRepository.findByEmail(loginRequest.getEmail())).thenReturn(Optional.of(testUser));
        when(passwordEncoder.matches(loginRequest.getPassword(), testUser.getPassword())).thenReturn(true);
        when(jwtUtil.generateToken(testUser)).thenReturn(testToken);
        when(permissionMapper.getPermissionsForProfile(UserProfile.TEACHER)).thenReturn(teacherPermissions);

        // Act
        LoginResponseDTO response = authenticationService.login(loginRequest);

        // Assert
        assertEquals(UserProfile.TEACHER, response.getProfile());
        assertEquals(teacherPermissions, response.getPermissions());
    }

    @Test
    void deveRealizarLoginComPerfilAdmin() {
        // Arrange
        testUser.setProfile(UserProfile.ADMIN);
        Set<String> adminPermissions = Set.of(
                "view_courses", "create_courses", "manage_users",
                "view_system_stats", "manage_settings"
        );

        when(userRepository.findByEmail(loginRequest.getEmail())).thenReturn(Optional.of(testUser));
        when(passwordEncoder.matches(loginRequest.getPassword(), testUser.getPassword())).thenReturn(true);
        when(jwtUtil.generateToken(testUser)).thenReturn(testToken);
        when(permissionMapper.getPermissionsForProfile(UserProfile.ADMIN)).thenReturn(adminPermissions);

        // Act
        LoginResponseDTO response = authenticationService.login(loginRequest);

        // Assert
        assertEquals(UserProfile.ADMIN, response.getProfile());
        assertEquals(adminPermissions, response.getPermissions());
    }
}
