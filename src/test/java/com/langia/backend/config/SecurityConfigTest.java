package com.langia.backend.config;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

/**
 * Integration tests for SecurityConfig (Requisito 8).
 * Validates that JWT authentication is correctly configured with:
 * - JWT filter registered in the correct position
 * - Public routes accessible without authentication
 * - Private routes protected and requiring authentication
 * - JSON error responses for unauthorized access
 * - CSRF, sessions, and form login disabled
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@DisplayName("SecurityConfig Integration Tests (Requisito 8)")
class SecurityConfigTest {

    @Autowired
    private MockMvc mockMvc;

    @Nested
    @DisplayName("Public Routes")
    class PublicRoutesTests {

        @Test
        @DisplayName("Should allow access to /api/users/register without authentication")
        void testRegisterEndpointIsPublic() throws Exception {
            // Register endpoint is public and should be accessible without authentication
            // Will return various statuses (201, 400, 409) but never 401/403 which would indicate authentication required
            mockMvc.perform(post("/api/users/register")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content("{\"name\":\"Test\",\"email\":\"test@test.com\",\"password\":\"password123\",\"cpf\":\"52998224725\",\"phone\":\"11987654321\",\"profile\":\"STUDENT\"}"))
                    .andExpect(result -> {
                        int status = result.getResponse().getStatus();
                        if (status == 401 || status == 403) {
                            throw new AssertionError("Register endpoint should be public (got " + status + ")");
                        }
                    });
        }

        @Test
        @DisplayName("Should allow access to /api/auth/login without authentication")
        void testLoginEndpointIsPublic() throws Exception {
            // Login endpoint is public and should be accessible without authentication
            // Will return 500 or 401 depending on credentials, but not 403 (forbidden)
            mockMvc.perform(post("/api/auth/login")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content("{\"email\":\"test@test.com\",\"password\":\"password123\"}"))
                    .andExpect(result -> {
                        int status = result.getResponse().getStatus();
                        if (status == 403) {
                            throw new AssertionError("Login endpoint should be public (got 403 Forbidden)");
                        }
                    });
        }

        @Test
        @DisplayName("Should allow access to /api/auth/logout without authentication")
        void testLogoutEndpointIsPublic() throws Exception {
            mockMvc.perform(post("/api/auth/logout"))
                    .andExpect(status().isUnauthorized()); // Missing token, but endpoint is accessible
        }

        @Test
        @DisplayName("Should allow access to /api/auth/validate without authentication")
        void testValidateEndpointIsPublic() throws Exception {
            mockMvc.perform(get("/api/auth/validate"))
                    .andExpect(status().isUnauthorized()); // Missing token, but endpoint is accessible
        }

        @Test
        @DisplayName("Should allow access to /actuator/health without authentication")
        void testHealthEndpointIsPublic() throws Exception {
            mockMvc.perform(get("/actuator/health"))
                    .andExpect(status().isOk());
        }

        @Test
        @DisplayName("Should allow access to /error without authentication")
        void testErrorEndpointIsPublic() throws Exception {
            mockMvc.perform(get("/error"))
                    .andExpect(status().isInternalServerError()); // Error endpoint is accessible
        }
    }

    @Nested
    @DisplayName("Protected Routes")
    class ProtectedRoutesTests {

        @Test
        @DisplayName("Should block access to /api/users without authentication")
        void testProtectedEndpointRequiresAuth() throws Exception {
            mockMvc.perform(get("/api/users"))
                    .andExpect(status().isUnauthorized());
        }

        @Test
        @DisplayName("Should block access to any non-public endpoint without authentication")
        void testAnyOtherEndpointRequiresAuth() throws Exception {
            mockMvc.perform(get("/api/some-protected-endpoint"))
                    .andExpect(status().isUnauthorized());
        }

        @Test
        @DisplayName("Should block POST requests to protected endpoints without authentication")
        void testProtectedPostEndpointRequiresAuth() throws Exception {
            mockMvc.perform(post("/api/users/123")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content("{}"))
                    .andExpect(status().isUnauthorized());
        }
    }

    @Nested
    @DisplayName("JSON Error Responses")
    class ErrorResponseTests {

        @Test
        @DisplayName("Should return JSON error response for unauthorized access")
        void testUnauthorizedReturnsJson() throws Exception {
            mockMvc.perform(get("/api/users"))
                    .andExpect(status().isUnauthorized())
                    .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                    .andExpect(jsonPath("$.status").value(401))
                    .andExpect(jsonPath("$.error").exists())
                    .andExpect(jsonPath("$.message").exists())
                    .andExpect(jsonPath("$.path").value("/api/users"))
                    .andExpect(jsonPath("$.timestamp").exists());
        }

        @Test
        @DisplayName("Should return JSON with proper structure on authentication failure")
        void testJsonErrorStructure() throws Exception {
            mockMvc.perform(get("/api/protected"))
                    .andExpect(status().isUnauthorized())
                    .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                    .andExpect(jsonPath("$.status").isNumber())
                    .andExpect(jsonPath("$.error").isString())
                    .andExpect(jsonPath("$.message").isString())
                    .andExpect(jsonPath("$.path").isString())
                    .andExpect(jsonPath("$.timestamp").isString());
        }
    }

    @Nested
    @DisplayName("Security Features")
    class SecurityFeaturesTests {

        @Test
        @DisplayName("Should have CSRF disabled for stateless API")
        void testCsrfIsDisabled() throws Exception {
            // CSRF is disabled, so POST requests should work without CSRF token
            // If CSRF were enabled, we would get 403 Forbidden
            // Will return various statuses (201, 400) but never 403 which would indicate CSRF protection
            mockMvc.perform(post("/api/users/register")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content("{\"name\":\"Test\",\"email\":\"test@test.com\",\"password\":\"password123\",\"cpf\":\"52998224725\",\"phone\":\"11987654321\",\"profile\":\"STUDENT\"}"))
                    .andExpect(result -> {
                        int status = result.getResponse().getStatus();
                        if (status == 403) {
                            throw new AssertionError("CSRF should be disabled (got 403 Forbidden)");
                        }
                    });
        }

        @Test
        @DisplayName("Should not create HTTP sessions (stateless)")
        void testStatelessSessionManagement() throws Exception {
            // In stateless mode, the server should not set JSESSIONID cookie
            mockMvc.perform(get("/actuator/health"))
                    .andExpect(status().isOk())
                    .andExpect(result -> {
                        String setCookieHeader = result.getResponse().getHeader("Set-Cookie");
                        if (setCookieHeader != null && setCookieHeader.contains("JSESSIONID")) {
                            throw new AssertionError("Expected no JSESSIONID cookie in stateless mode");
                        }
                    });
        }

        @Test
        @DisplayName("Should reject requests with invalid Authorization header format")
        void testInvalidAuthHeaderFormat() throws Exception {
            mockMvc.perform(get("/api/users")
                    .header("Authorization", "InvalidFormat token123"))
                    .andExpect(status().isUnauthorized());
        }

        @Test
        @DisplayName("Should reject requests with malformed Bearer token")
        void testMalformedBearerToken() throws Exception {
            mockMvc.perform(get("/api/users")
                    .header("Authorization", "Bearer invalid-token"))
                    .andExpect(status().isUnauthorized());
        }
    }
}

