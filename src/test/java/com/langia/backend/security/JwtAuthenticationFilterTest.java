package com.langia.backend.security;

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
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.core.context.SecurityContextHolder;

import com.langia.backend.dto.UserSessionDTO;
import com.langia.backend.model.UserProfile;
import com.langia.backend.service.JwtTokenService;
import com.langia.backend.service.UserSessionService;

import jakarta.servlet.FilterChain;

@ExtendWith(MockitoExtension.class)
@SuppressWarnings("null")
class JwtAuthenticationFilterTest {

    @Mock
    private JwtTokenService jwtTokenService;

    @Mock
    private UserSessionService userSessionService;

    @Mock
    private FilterChain filterChain;

    @InjectMocks
    private JwtAuthenticationFilter jwtAuthenticationFilter;

    private MockHttpServletRequest request;
    private MockHttpServletResponse response;

    @BeforeEach
    void setUp() {
        request = new MockHttpServletRequest();
        response = new MockHttpServletResponse();
        SecurityContextHolder.clearContext();
    }

    @Test
    void doFilterInternal_shouldContinueWhenNoAuthorizationHeader() throws Exception {
        request.removeHeader("Authorization");

        jwtAuthenticationFilter.doFilterInternal(request, response, filterChain);

        verify(filterChain).doFilter(request, response);
        verify(jwtTokenService, never()).validateToken(anyString());
        verify(userSessionService, never()).getSession(anyString());
    }

    @Test
    void doFilterInternal_shouldContinueWhenAuthorizationHeaderDoesNotStartWithBearer() throws Exception {
        request.addHeader("Authorization", "Basic dXNlcjpwYXNz");

        jwtAuthenticationFilter.doFilterInternal(request, response, filterChain);

        verify(filterChain).doFilter(request, response);
        verify(jwtTokenService, never()).validateToken(anyString());
        verify(userSessionService, never()).getSession(anyString());
    }

    @Test
    void doFilterInternal_shouldContinueWhenTokenIsInvalid() throws Exception {
        String token = "invalid.token";
        request.addHeader("Authorization", "Bearer " + token);

        when(jwtTokenService.validateToken(token)).thenReturn(false);

        jwtAuthenticationFilter.doFilterInternal(request, response, filterChain);

        verify(jwtTokenService).validateToken(token);
        verify(userSessionService, never()).getSession(anyString());
        verify(filterChain).doFilter(request, response);
    }

    @Test
    void doFilterInternal_shouldContinueWhenSessionNotFound() throws Exception {
        String token = "valid.token";
        request.addHeader("Authorization", "Bearer " + token);

        when(jwtTokenService.validateToken(token)).thenReturn(true);
        when(userSessionService.getSession(token)).thenReturn(null);

        jwtAuthenticationFilter.doFilterInternal(request, response, filterChain);

        verify(jwtTokenService).validateToken(token);
        verify(userSessionService).getSession(token);
        verify(filterChain).doFilter(request, response);
    }

    @Test
    void doFilterInternal_shouldSetAuthenticationWhenTokenAndSessionAreValid() throws Exception {
        String token = "valid.token";
        UUID userId = UUID.randomUUID();
        UserSessionDTO session = UserSessionDTO.builder()
                .userId(userId)
                .name("Test User")
                .email("test@example.com")
                .profile(UserProfile.STUDENT)
                .permissions(Set.of("permission1", "permission2"))
                .expiresAt(System.currentTimeMillis() + 3600000L)
                .build();

        request.addHeader("Authorization", "Bearer " + token);

        when(jwtTokenService.validateToken(token)).thenReturn(true);
        when(userSessionService.getSession(token)).thenReturn(session);

        jwtAuthenticationFilter.doFilterInternal(request, response, filterChain);

        verify(jwtTokenService).validateToken(token);
        verify(userSessionService).getSession(token);
        verify(filterChain).doFilter(request, response);

        // Verify authentication was set
        assert SecurityContextHolder.getContext().getAuthentication() != null;
        assert SecurityContextHolder.getContext().getAuthentication().getPrincipal().equals(userId);
    }

    @Test
    void doFilterInternal_shouldExtractTokenCorrectly() throws Exception {
        String token = "test.jwt.token";
        UUID userId = UUID.randomUUID();
        UserSessionDTO session = UserSessionDTO.builder()
                .userId(userId)
                .name("Test User")
                .email("test@example.com")
                .profile(UserProfile.STUDENT)
                .permissions(Set.of("permission1"))
                .expiresAt(System.currentTimeMillis() + 3600000L)
                .build();

        request.addHeader("Authorization", "Bearer " + token);

        when(jwtTokenService.validateToken(token)).thenReturn(true);
        when(userSessionService.getSession(token)).thenReturn(session);

        jwtAuthenticationFilter.doFilterInternal(request, response, filterChain);

        verify(jwtTokenService).validateToken(token);
        verify(userSessionService).getSession(token);
    }

    @Test
    void doFilterInternal_shouldHandleExceptionGracefully() throws Exception {
        String token = "test.token";
        request.addHeader("Authorization", "Bearer " + token);

        when(jwtTokenService.validateToken(token)).thenThrow(new RuntimeException("Service error"));

        jwtAuthenticationFilter.doFilterInternal(request, response, filterChain);

        verify(filterChain).doFilter(request, response);
    }

    @Test
    void doFilterInternal_shouldNotSetAuthenticationWhenAlreadySet() throws Exception {
        String token = "valid.token";
        UUID userId = UUID.randomUUID();
        UserSessionDTO session = UserSessionDTO.builder()
                .userId(userId)
                .name("Test User")
                .email("test@example.com")
                .profile(UserProfile.STUDENT)
                .permissions(Set.of("permission1"))
                .expiresAt(System.currentTimeMillis() + 3600000L)
                .build();

        request.addHeader("Authorization", "Bearer " + token);

        // Set authentication first
        org.springframework.security.authentication.UsernamePasswordAuthenticationToken existingAuth = new org.springframework.security.authentication.UsernamePasswordAuthenticationToken(
                UUID.randomUUID(), null, null);
        SecurityContextHolder.getContext().setAuthentication(existingAuth);

        when(jwtTokenService.validateToken(token)).thenReturn(true);
        when(userSessionService.getSession(token)).thenReturn(session);

        jwtAuthenticationFilter.doFilterInternal(request, response, filterChain);

        verify(filterChain).doFilter(request, response);
        // Authentication should remain the same
        assert SecurityContextHolder.getContext().getAuthentication() == existingAuth;
    }
}
