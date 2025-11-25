package com.langia.backend.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.langia.backend.dto.LoginRequestDTO;
import com.langia.backend.dto.LoginResponseDTO;
import com.langia.backend.dto.SessionData;
import com.langia.backend.exception.InvalidCredentialsException;
import com.langia.backend.service.AuthenticationService;

import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;

/**
 * Controller REST para endpoints de autenticação.
 * Responsável por receber requisições HTTP, validar dados de entrada,
 * chamar serviços apropriados e retornar respostas HTTP adequadas.
 */
@RestController
@RequestMapping("/api/auth")
@Slf4j
public class AuthenticationController {

    @Autowired
    private AuthenticationService authenticationService;

    /**
     * Endpoint de login.
     * Recebe credenciais do usuário e retorna token JWT se válidas.
     *
     * @param loginRequest credenciais de login (email e senha)
     * @return resposta com token e dados do usuário (200) ou erro (401)
     */
    @PostMapping("/login")
    public ResponseEntity<?> login(@Valid @RequestBody LoginRequestDTO loginRequest) {
        try {
            log.info("Requisição de login recebida para email: {}", loginRequest.getEmail());

            LoginResponseDTO response = authenticationService.login(loginRequest);

            log.info("Login bem-sucedido via API para: {}", loginRequest.getEmail());
            return ResponseEntity.ok(response);

        } catch (InvalidCredentialsException e) {
            log.warn("Tentativa de login falhou para email: {}", loginRequest.getEmail());
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(new ErrorResponse("Invalid credentials"));

        } catch (Exception e) {
            log.error("Erro inesperado durante login para email: {}", loginRequest.getEmail(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ErrorResponse("An unexpected error occurred"));
        }
    }

    /**
     * Endpoint de logout.
     * Invalida a sessão do usuário removendo-a do Redis.
     *
     * @param authorizationHeader header Authorization contendo o token Bearer
     * @return resposta vazia (204) ou erro (401)
     */
    @PostMapping("/logout")
    public ResponseEntity<?> logout(@RequestHeader(value = "Authorization", required = false) String authorizationHeader) {
        try {
            String token = extractToken(authorizationHeader);

            if (token == null) {
                log.warn("Tentativa de logout sem token válido");
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body(new ErrorResponse("Missing or invalid Authorization header"));
            }

            boolean loggedOut = authenticationService.logout(token);

            if (loggedOut) {
                log.info("Logout realizado com sucesso via API");
                return ResponseEntity.noContent().build();
            } else {
                log.warn("Tentativa de logout com token inexistente ou expirado");
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body(new ErrorResponse("Session not found or already expired"));
            }

        } catch (Exception e) {
            log.error("Erro inesperado durante logout", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ErrorResponse("An unexpected error occurred"));
        }
    }

    /**
     * Endpoint de validação de sessão.
     * Verifica se um token ainda é válido e retorna dados da sessão.
     *
     * @param authorizationHeader header Authorization contendo o token Bearer
     * @return dados da sessão (200) ou erro (401)
     */
    @GetMapping("/validate")
    public ResponseEntity<?> validateSession(@RequestHeader(value = "Authorization", required = false) String authorizationHeader) {
        try {
            String token = extractToken(authorizationHeader);

            if (token == null) {
                log.warn("Tentativa de validação sem token válido");
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body(new ErrorResponse("Missing or invalid Authorization header"));
            }

            SessionData sessionData = authenticationService.validateSession(token);

            if (sessionData != null) {
                log.debug("Sessão validada com sucesso via API");
                return ResponseEntity.ok(new SessionValidationResponse(true, sessionData));
            } else {
                log.warn("Tentativa de validação com token inválido ou sessão expirada");
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body(new SessionValidationResponse(false, null));
            }

        } catch (Exception e) {
            log.error("Erro inesperado durante validação de sessão", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ErrorResponse("An unexpected error occurred"));
        }
    }

    /**
     * Endpoint para renovar sessão.
     * Estende o tempo de expiração de uma sessão ativa.
     *
     * @param authorizationHeader header Authorization contendo o token Bearer
     * @return confirmação de renovação (200) ou erro (401)
     */
    @PostMapping("/renew")
    public ResponseEntity<?> renewSession(@RequestHeader(value = "Authorization", required = false) String authorizationHeader) {
        try {
            String token = extractToken(authorizationHeader);

            if (token == null) {
                log.warn("Tentativa de renovação sem token válido");
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body(new ErrorResponse("Missing or invalid Authorization header"));
            }

            boolean renewed = authenticationService.renewSession(token);

            if (renewed) {
                log.debug("Sessão renovada com sucesso via API");
                return ResponseEntity.ok(new MessageResponse("Session renewed successfully"));
            } else {
                log.warn("Tentativa de renovação com token inválido ou sessão inexistente");
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body(new ErrorResponse("Invalid token or session not found"));
            }

        } catch (Exception e) {
            log.error("Erro inesperado durante renovação de sessão", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ErrorResponse("An unexpected error occurred"));
        }
    }

    /**
     * Extrai o token JWT do header Authorization.
     * Espera formato: "Bearer <token>"
     *
     * @param authorizationHeader conteúdo do header Authorization
     * @return token extraído ou null se formato inválido
     */
    private String extractToken(String authorizationHeader) {
        if (authorizationHeader != null && authorizationHeader.startsWith("Bearer ")) {
            return authorizationHeader.substring(7);
        }
        return null;
    }

    /**
     * DTO para respostas de erro.
     */
    private record ErrorResponse(String message) {}

    /**
     * DTO para respostas de validação de sessão.
     */
    private record SessionValidationResponse(boolean valid, SessionData session) {}

    /**
     * DTO para respostas de mensagem simples.
     */
    private record MessageResponse(String message) {}
}
