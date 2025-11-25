package com.langia.backend.filter;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Set;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockFilterChain;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;

import com.langia.backend.dto.SessionData;
import com.langia.backend.model.UserProfile;
import com.langia.backend.service.AuthenticationService;

/**
 * Testes para o filtro de autenticação JWT.
 */
@ExtendWith(MockitoExtension.class)
class JwtAuthenticationFilterTest {

    @Mock
    private AuthenticationService authenticationService;

    @InjectMocks
    private JwtAuthenticationFilter jwtAuthenticationFilter;

    private MockHttpServletRequest request;
    private MockHttpServletResponse response;
    private MockFilterChain filterChain;
    private SessionData validSessionData;
    private String validToken;

    @BeforeEach
    void setUp() {
        request = new MockHttpServletRequest();
        response = new MockHttpServletResponse();
        filterChain = new MockFilterChain();

        validToken = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.valid_token";

        UUID userId = UUID.randomUUID();
        validSessionData = SessionData.builder()
                .userId(userId)
                .name("Test User")
                .email("test@example.com")
                .profile(UserProfile.STUDENT)
                .permissions(Set.of("view_courses", "view_lessons"))
                .createdAt(System.currentTimeMillis())
                .build();

        // Limpa o contexto de segurança antes de cada teste
        SecurityContextHolder.clearContext();
    }

    // ========== Testes de Extração de Token ==========

    @Test
    void deveExtrairTokenComSucessoDoHeaderBearer() throws Exception {
        // Arrange
        request.addHeader("Authorization", "Bearer " + validToken);
        when(authenticationService.validateSession(validToken)).thenReturn(validSessionData);

        // Act
        jwtAuthenticationFilter.doFilterInternal(request, response, filterChain);

        // Assert
        verify(authenticationService).validateSession(validToken);
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        assertNotNull(auth);
        assertNotNull(auth.getPrincipal());
    }

    @Test
    void deveContinuarSemAutenticacaoQuandoNaoHouverToken() throws Exception {
        // Arrange - sem header Authorization

        // Act
        jwtAuthenticationFilter.doFilterInternal(request, response, filterChain);

        // Assert
        verify(authenticationService, never()).validateSession(anyString());
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        assertNull(auth);
    }

    @Test
    void deveContinuarSemAutenticacaoQuandoHeaderNaoForBearer() throws Exception {
        // Arrange
        request.addHeader("Authorization", "Basic dXNlcjpwYXNz");

        // Act
        jwtAuthenticationFilter.doFilterInternal(request, response, filterChain);

        // Assert
        verify(authenticationService, never()).validateSession(anyString());
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        assertNull(auth);
    }

    @Test
    void deveContinuarSemAutenticacaoQuandoHeaderForVazio() throws Exception {
        // Arrange
        request.addHeader("Authorization", "");

        // Act
        jwtAuthenticationFilter.doFilterInternal(request, response, filterChain);

        // Assert
        verify(authenticationService, never()).validateSession(anyString());
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        assertNull(auth);
    }

    // ========== Testes de Validação de Sessão ==========

    @Test
    void deveInjetarContextoDeSegurancaParaSessaoValida() throws Exception {
        // Arrange
        request.addHeader("Authorization", "Bearer " + validToken);
        when(authenticationService.validateSession(validToken)).thenReturn(validSessionData);

        // Act
        jwtAuthenticationFilter.doFilterInternal(request, response, filterChain);

        // Assert
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        assertNotNull(auth);
        assertNotNull(auth.getPrincipal());

        SessionData principal = (SessionData) auth.getPrincipal();
        assertNotNull(principal);
        assertNotNull(principal.getUserId());
        assertNotNull(principal.getEmail());
        assertNotNull(principal.getProfile());
    }

    @Test
    void naoDeveInjetarContextoParaSessaoInvalida() throws Exception {
        // Arrange
        request.addHeader("Authorization", "Bearer " + validToken);
        when(authenticationService.validateSession(validToken)).thenReturn(null);

        // Act
        jwtAuthenticationFilter.doFilterInternal(request, response, filterChain);

        // Assert
        verify(authenticationService).validateSession(validToken);
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        assertNull(auth);
    }

    @Test
    void naoDeveInjetarContextoParaTokenExpirado() throws Exception {
        // Arrange
        String expiredToken = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.expired_token";
        request.addHeader("Authorization", "Bearer " + expiredToken);
        when(authenticationService.validateSession(expiredToken)).thenReturn(null);

        // Act
        jwtAuthenticationFilter.doFilterInternal(request, response, filterChain);

        // Assert
        verify(authenticationService).validateSession(expiredToken);
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        assertNull(auth);
    }

    // ========== Testes de Cadeia de Filtros ==========

    @Test
    void deveContinuarCadeiaAposSucessoNaAutenticacao() throws Exception {
        // Arrange
        request.addHeader("Authorization", "Bearer " + validToken);
        when(authenticationService.validateSession(validToken)).thenReturn(validSessionData);

        // Act
        jwtAuthenticationFilter.doFilterInternal(request, response, filterChain);

        // Assert
        // Verifica que a cadeia continuou (filtro chamou doFilter)
        assertNotNull(filterChain.getRequest());
        assertNotNull(filterChain.getResponse());
    }

    @Test
    void deveContinuarCadeiaQuandoNaoHouverToken() throws Exception {
        // Arrange - sem token

        // Act
        jwtAuthenticationFilter.doFilterInternal(request, response, filterChain);

        // Assert
        // Cadeia deve continuar mesmo sem autenticação
        assertNotNull(filterChain.getRequest());
        assertNotNull(filterChain.getResponse());
    }

    @Test
    void deveContinuarCadeiaMesmoComTokenInvalido() throws Exception {
        // Arrange
        request.addHeader("Authorization", "Bearer " + validToken);
        when(authenticationService.validateSession(validToken)).thenReturn(null);

        // Act
        jwtAuthenticationFilter.doFilterInternal(request, response, filterChain);

        // Assert
        // Cadeia continua - Spring Security bloqueará se rota for protegida
        assertNotNull(filterChain.getRequest());
        assertNotNull(filterChain.getResponse());
    }

    // ========== Testes de Tratamento de Erros ==========

    @Test
    void deveTratarExcecaoELimparContexto() throws Exception {
        // Arrange
        request.addHeader("Authorization", "Bearer " + validToken);
        when(authenticationService.validateSession(validToken))
                .thenThrow(new RuntimeException("Redis connection error"));

        // Act
        jwtAuthenticationFilter.doFilterInternal(request, response, filterChain);

        // Assert
        // Contexto deve estar limpo após exceção
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        assertNull(auth);

        // Cadeia deve continuar mesmo com erro
        assertNotNull(filterChain.getRequest());
    }

    // ========== Testes de Diferentes Perfis ==========

    @Test
    void deveInjetarContextoParaUsuarioStudent() throws Exception {
        // Arrange
        validSessionData.setProfile(UserProfile.STUDENT);
        request.addHeader("Authorization", "Bearer " + validToken);
        when(authenticationService.validateSession(validToken)).thenReturn(validSessionData);

        // Act
        jwtAuthenticationFilter.doFilterInternal(request, response, filterChain);

        // Assert
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        SessionData principal = (SessionData) auth.getPrincipal();
        assertNotNull(principal);
        assertNotNull(principal.getProfile());
    }

    @Test
    void deveInjetarContextoParaUsuarioTeacher() throws Exception {
        // Arrange
        validSessionData.setProfile(UserProfile.TEACHER);
        request.addHeader("Authorization", "Bearer " + validToken);
        when(authenticationService.validateSession(validToken)).thenReturn(validSessionData);

        // Act
        jwtAuthenticationFilter.doFilterInternal(request, response, filterChain);

        // Assert
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        SessionData principal = (SessionData) auth.getPrincipal();
        assertNotNull(principal);
        assertNotNull(principal.getProfile());
    }

    @Test
    void deveInjetarContextoParaUsuarioAdmin() throws Exception {
        // Arrange
        validSessionData.setProfile(UserProfile.ADMIN);
        request.addHeader("Authorization", "Bearer " + validToken);
        when(authenticationService.validateSession(validToken)).thenReturn(validSessionData);

        // Act
        jwtAuthenticationFilter.doFilterInternal(request, response, filterChain);

        // Assert
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        SessionData principal = (SessionData) auth.getPrincipal();
        assertNotNull(principal);
        assertNotNull(principal.getProfile());
    }

    // ========== Testes de Casos Especiais ==========

    @Test
    void deveExtrairTokenComEspacosExtras() throws Exception {
        // Arrange
        request.addHeader("Authorization", "Bearer  " + validToken); // Dois espaços
        // Não vai validar pois substring(7) pegará " " + token
        when(authenticationService.validateSession(anyString())).thenReturn(null);

        // Act
        jwtAuthenticationFilter.doFilterInternal(request, response, filterChain);

        // Assert
        // Com dois espaços, o token extraído terá um espaço no início
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        assertNull(auth);
    }

    @Test
    void deveRejeitarHeaderBearerSemToken() throws Exception {
        // Arrange
        request.addHeader("Authorization", "Bearer ");

        // Act
        jwtAuthenticationFilter.doFilterInternal(request, response, filterChain);

        // Assert
        verify(authenticationService, never()).validateSession(anyString());
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        assertNull(auth);
    }

    @Test
    void deveRejeitarHeaderBearerMinusculo() throws Exception {
        // Arrange
        request.addHeader("Authorization", "bearer " + validToken);

        // Act
        jwtAuthenticationFilter.doFilterInternal(request, response, filterChain);

        // Assert
        verify(authenticationService, never()).validateSession(anyString());
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        assertNull(auth);
    }

    // ========== Testes de Mapeamento de Permissões para Authorities ==========

    @Test
    void deveMappearPermissoesParaGrantedAuthorities() throws Exception {
        // Arrange
        validSessionData.setPermissions(Set.of("view_courses", "view_lessons", "submit_exercises"));
        request.addHeader("Authorization", "Bearer " + validToken);
        when(authenticationService.validateSession(validToken)).thenReturn(validSessionData);

        // Act
        jwtAuthenticationFilter.doFilterInternal(request, response, filterChain);

        // Assert
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        assertNotNull(auth);
        assertNotNull(auth.getAuthorities());
        assertEquals(3, auth.getAuthorities().size());

        // Verifica que todas as permissões foram convertidas para GrantedAuthority
        var authorityNames = auth.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .toList();

        assertTrue(authorityNames.contains("view_courses"));
        assertTrue(authorityNames.contains("view_lessons"));
        assertTrue(authorityNames.contains("submit_exercises"));
    }

    @Test
    void deveInjetarAuthoritiesVaziaQuandoSemPermissoes() throws Exception {
        // Arrange
        validSessionData.setPermissions(null); // Sem permissões
        request.addHeader("Authorization", "Bearer " + validToken);
        when(authenticationService.validateSession(validToken)).thenReturn(validSessionData);

        // Act
        jwtAuthenticationFilter.doFilterInternal(request, response, filterChain);

        // Assert
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        assertNotNull(auth);
        assertNotNull(auth.getAuthorities());
        assertEquals(0, auth.getAuthorities().size());
    }

    @Test
    void deveMappearPermissoesDiferentesPorPerfil() throws Exception {
        // Arrange - Perfil ADMIN com mais permissões
        Set<String> adminPermissions = Set.of(
                "view_courses", "create_courses", "manage_users", "view_system_stats"
        );
        validSessionData.setProfile(UserProfile.ADMIN);
        validSessionData.setPermissions(adminPermissions);

        request.addHeader("Authorization", "Bearer " + validToken);
        when(authenticationService.validateSession(validToken)).thenReturn(validSessionData);

        // Act
        jwtAuthenticationFilter.doFilterInternal(request, response, filterChain);

        // Assert
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        assertNotNull(auth);
        assertEquals(4, auth.getAuthorities().size());

        var authorityNames = auth.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .toList();

        assertTrue(authorityNames.contains("manage_users"));
        assertTrue(authorityNames.contains("view_system_stats"));
    }

    @Test
    void devePermitirAuthorizationComAuthorities() throws Exception {
        // Arrange
        validSessionData.setPermissions(Set.of("manage_users"));
        request.addHeader("Authorization", "Bearer " + validToken);
        when(authenticationService.validateSession(validToken)).thenReturn(validSessionData);

        // Act
        jwtAuthenticationFilter.doFilterInternal(request, response, filterChain);

        // Assert
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        assertNotNull(auth);

        // Verifica que a permissão específica está presente
        boolean hasManageUsersAuthority = auth.getAuthorities().stream()
                .anyMatch(authority -> authority.getAuthority().equals("manage_users"));

        assertTrue(hasManageUsersAuthority, "Deve ter a authority 'manage_users'");
    }
}
