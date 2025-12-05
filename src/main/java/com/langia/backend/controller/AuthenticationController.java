package com.langia.backend.controller;

import java.time.Duration;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.langia.backend.config.AuthCookieProperties;
import com.langia.backend.dto.LoginRequestDTO;
import com.langia.backend.dto.LoginResponseDTO;
import com.langia.backend.dto.MessageResponse;
import com.langia.backend.dto.SessionData;
import com.langia.backend.dto.SessionValidationResponse;
import com.langia.backend.exception.InvalidCredentialsException;
import com.langia.backend.exception.InvalidSessionException;
import com.langia.backend.exception.RateLimitExceededException;
import com.langia.backend.service.AuthenticationService;
import com.langia.backend.service.LoginRateLimitService;
import com.langia.backend.util.TokenExtractor;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * Controller REST para endpoints de autenticação.
 * Responsável por receber requisições HTTP e delegar para o serviço.
 * Tratamento de exceções centralizado via GlobalExceptionHandler.
 */
@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
@Slf4j
public class AuthenticationController {

    private final AuthenticationService authenticationService;
    private final TokenExtractor tokenExtractor;
    private final AuthCookieProperties cookieProperties;
    private final LoginRateLimitService loginRateLimitService;

    @Value("${jwt.expiration}")
    private Long jwtExpirationMs;

    /**
     * Endpoint de login.
     * Recebe credenciais do usuário e retorna token JWT se válidas.
     * O token é enviado tanto no corpo da resposta quanto em um cookie HttpOnly.
     * Inclui rate limiting para proteção contra ataques de força bruta.
     *
     * @param loginRequest credenciais de login (email e senha)
     * @param request requisição HTTP para extração do IP
     * @return resposta com token e dados do usuário
     * @throws RateLimitExceededException se o limite de tentativas for excedido
     */
    @PostMapping("/login")
    public ResponseEntity<LoginResponseDTO> login(
            @Valid @RequestBody LoginRequestDTO loginRequest,
            HttpServletRequest request) {

        String clientIp = getClientIp(request);
        String email = loginRequest.getEmail();

        log.info("Requisição de login recebida para email: {} (IP: {})", email, clientIp);

        // Verifica rate limiting por IP
        if (loginRateLimitService.isIpBlocked(clientIp)) {
            long retryAfter = loginRateLimitService.getTimeUntilReset(clientIp);
            log.warn("Login bloqueado por rate limit de IP: {} (retry em {}s)", clientIp, retryAfter);
            throw new RateLimitExceededException(retryAfter);
        }

        // Verifica rate limiting por email
        if (loginRateLimitService.isEmailBlocked(email)) {
            long retryAfter = loginRateLimitService.getTimeUntilReset(clientIp);
            log.warn("Login bloqueado por rate limit de email: {} (retry em {}s)", email, retryAfter);
            throw new RateLimitExceededException(retryAfter);
        }

        try {
            LoginResponseDTO response = authenticationService.login(loginRequest);

            // Login bem-sucedido: reseta contadores
            loginRateLimitService.resetOnSuccess(clientIp, email);

            // Cria cookie HttpOnly com o token JWT
            ResponseCookie authCookie = buildAuthCookie(response.getToken(), Duration.ofMillis(jwtExpirationMs));

            log.info("Login bem-sucedido via API para: {}", email);
            return ResponseEntity.ok()
                    .header(HttpHeaders.SET_COOKIE, authCookie.toString())
                    .body(response);

        } catch (InvalidCredentialsException e) {
            // Login falhou: registra tentativa
            loginRateLimitService.recordFailedAttempt(clientIp, email);
            int remainingIp = loginRateLimitService.getRemainingAttemptsForIp(clientIp);
            int remainingEmail = loginRateLimitService.getRemainingAttemptsForEmail(email);
            log.warn("Falha de login para email: {} (tentativas restantes - IP: {}, Email: {})",
                    email, remainingIp, remainingEmail);
            throw e;
        }
    }

    /**
     * Endpoint de logout.
     * Invalida a sessão do usuário removendo-a do Redis e limpa o cookie de autenticação.
     *
     * @param request requisição HTTP para extração do token (cookie ou header)
     * @return resposta vazia (204) com cookie limpo
     */
    @PostMapping("/logout")
    public ResponseEntity<Void> logout(HttpServletRequest request) {
        String token = extractTokenOrThrow(request);

        boolean loggedOut = authenticationService.logout(token);
        if (!loggedOut) {
            throw new InvalidSessionException("Session not found or already expired");
        }

        // Limpa o cookie de autenticação
        ResponseCookie clearCookie = buildClearCookie();

        log.info("Logout realizado com sucesso via API");
        return ResponseEntity.noContent()
                .header(HttpHeaders.SET_COOKIE, clearCookie.toString())
                .build();
    }

    /**
     * Endpoint de validação de sessão.
     * Verifica se um token ainda é válido e retorna dados da sessão.
     *
     * @param request requisição HTTP para extração do token (cookie ou header)
     * @return dados da sessão
     */
    @GetMapping("/validate")
    public ResponseEntity<SessionValidationResponse> validateSession(HttpServletRequest request) {
        String token = tokenExtractor.extractFromRequest(request);
        if (token == null) {
            log.warn("Tentativa de validação sem token");
            return ResponseEntity.status(401).body(new SessionValidationResponse(false, null));
        }

        SessionData sessionData = authenticationService.validateSession(token);
        if (sessionData == null) {
            log.warn("Tentativa de validação com token inválido ou sessão expirada");
            return ResponseEntity.status(401).body(new SessionValidationResponse(false, null));
        }

        log.debug("Sessão validada com sucesso via API");
        return ResponseEntity.ok(new SessionValidationResponse(true, sessionData));
    }

    /**
     * Endpoint para renovar sessão.
     * Estende o tempo de expiração de uma sessão ativa.
     *
     * @param request requisição HTTP para extração do token (cookie ou header)
     * @return confirmação de renovação
     */
    @PostMapping("/renew")
    public ResponseEntity<MessageResponse> renewSession(HttpServletRequest request) {
        String token = extractTokenOrThrow(request);

        boolean renewed = authenticationService.renewSession(token);
        if (!renewed) {
            throw new InvalidSessionException("Invalid token or session not found");
        }

        log.debug("Sessão renovada com sucesso via API");
        return ResponseEntity.ok(new MessageResponse("Session renewed successfully"));
    }

    // ========== Helper Methods ==========

    /**
     * Constrói um cookie de autenticação com as configurações padrão.
     *
     * @param token valor do token JWT
     * @param maxAge duração do cookie
     * @return cookie configurado
     */
    private ResponseCookie buildAuthCookie(String token, Duration maxAge) {
        return ResponseCookie.from(cookieProperties.getName(), token)
                .httpOnly(true)
                .secure(cookieProperties.isSecure())
                .sameSite(cookieProperties.getSameSite())
                .path("/")
                .maxAge(maxAge)
                .build();
    }

    /**
     * Constrói um cookie vazio para limpar a autenticação.
     *
     * @return cookie de limpeza
     */
    private ResponseCookie buildClearCookie() {
        return buildAuthCookie("", Duration.ZERO);
    }

    /**
     * Extrai o token da requisição ou lança exceção se não encontrado.
     *
     * @param request requisição HTTP
     * @return token extraído
     * @throws InvalidSessionException se nenhum token for encontrado
     */
    private String extractTokenOrThrow(HttpServletRequest request) {
        String token = tokenExtractor.extractFromRequest(request);
        if (token == null) {
            throw new InvalidSessionException("No token provided");
        }
        return token;
    }

    /**
     * Extrai o IP real do cliente, considerando headers de proxy.
     *
     * @param request requisição HTTP
     * @return IP do cliente
     */
    private String getClientIp(HttpServletRequest request) {
        String xForwardedFor = request.getHeader("X-Forwarded-For");
        if (xForwardedFor != null && !xForwardedFor.isEmpty()) {
            // X-Forwarded-For pode conter múltiplos IPs, pega o primeiro (cliente original)
            return xForwardedFor.split(",")[0].trim();
        }

        String xRealIp = request.getHeader("X-Real-IP");
        if (xRealIp != null && !xRealIp.isEmpty()) {
            return xRealIp;
        }

        return request.getRemoteAddr();
    }
}
