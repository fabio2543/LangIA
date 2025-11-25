package com.langia.backend.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.langia.backend.dto.LoginRequestDTO;
import com.langia.backend.dto.LoginResponseDTO;
import com.langia.backend.dto.MessageResponse;
import com.langia.backend.dto.SessionData;
import com.langia.backend.dto.SessionValidationResponse;
import com.langia.backend.exception.InvalidSessionException;
import com.langia.backend.service.AuthenticationService;
import com.langia.backend.util.TokenExtractor;

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

    /**
     * Endpoint de login.
     * Recebe credenciais do usuário e retorna token JWT se válidas.
     *
     * @param loginRequest credenciais de login (email e senha)
     * @return resposta com token e dados do usuário
     */
    @PostMapping("/login")
    public ResponseEntity<LoginResponseDTO> login(@Valid @RequestBody LoginRequestDTO loginRequest) {
        log.info("Requisição de login recebida para email: {}", loginRequest.getEmail());
        LoginResponseDTO response = authenticationService.login(loginRequest);
        log.info("Login bem-sucedido via API para: {}", loginRequest.getEmail());
        return ResponseEntity.ok(response);
    }

    /**
     * Endpoint de logout.
     * Invalida a sessão do usuário removendo-a do Redis.
     *
     * @param authorizationHeader header Authorization contendo o token Bearer
     * @return resposta vazia (204)
     */
    @PostMapping("/logout")
    public ResponseEntity<Void> logout(
            @RequestHeader(value = "Authorization", required = false) String authorizationHeader) {
        String token = tokenExtractor.extract(authorizationHeader);

        boolean loggedOut = authenticationService.logout(token);
        if (!loggedOut) {
            throw new InvalidSessionException("Session not found or already expired");
        }

        log.info("Logout realizado com sucesso via API");
        return ResponseEntity.noContent().build();
    }

    /**
     * Endpoint de validação de sessão.
     * Verifica se um token ainda é válido e retorna dados da sessão.
     *
     * @param authorizationHeader header Authorization contendo o token Bearer
     * @return dados da sessão
     */
    @GetMapping("/validate")
    public ResponseEntity<SessionValidationResponse> validateSession(
            @RequestHeader(value = "Authorization", required = false) String authorizationHeader) {
        String token = tokenExtractor.extract(authorizationHeader);

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
     * @param authorizationHeader header Authorization contendo o token Bearer
     * @return confirmação de renovação
     */
    @PostMapping("/renew")
    public ResponseEntity<MessageResponse> renewSession(
            @RequestHeader(value = "Authorization", required = false) String authorizationHeader) {
        String token = tokenExtractor.extract(authorizationHeader);

        boolean renewed = authenticationService.renewSession(token);
        if (!renewed) {
            throw new InvalidSessionException("Invalid token or session not found");
        }

        log.debug("Sessão renovada com sucesso via API");
        return ResponseEntity.ok(new MessageResponse("Session renewed successfully"));
    }
}
