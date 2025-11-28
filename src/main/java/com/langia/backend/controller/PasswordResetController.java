package com.langia.backend.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.langia.backend.dto.ForgotPasswordRequestDTO;
import com.langia.backend.dto.ForgotPasswordResponseDTO;
import com.langia.backend.dto.ResetPasswordRequestDTO;
import com.langia.backend.dto.ResetPasswordResponseDTO;
import com.langia.backend.dto.ValidateTokenResponseDTO;
import com.langia.backend.service.PasswordResetService;
import com.langia.backend.util.IpAddressUtil;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * Controller para operações de recuperação de senha.
 *
 * Endpoints:
 * - POST /api/auth/password/forgot - Solicitar recuperação
 * - GET /api/auth/password/reset/{token} - Validar token
 * - POST /api/auth/password/reset - Redefinir senha
 */
@RestController
@RequestMapping("/api/auth/password")
@RequiredArgsConstructor
@Slf4j
public class PasswordResetController {

    private final PasswordResetService passwordResetService;

    /**
     * Solicita recuperação de senha.
     * Sempre retorna 200 OK para não revelar se o email existe.
     *
     * @param request     DTO com email
     * @param httpRequest Request HTTP para extrair IP
     * @return Resposta genérica de sucesso
     */
    @PostMapping("/forgot")
    public ResponseEntity<ForgotPasswordResponseDTO> forgotPassword(
            @Valid @RequestBody ForgotPasswordRequestDTO request,
            HttpServletRequest httpRequest) {

        String ipAddress = IpAddressUtil.getClientIp(httpRequest);
        log.info("Password reset requested for email: {} from IP: {}",
                maskEmail(request.getEmail()), ipAddress);

        passwordResetService.requestPasswordReset(request.getEmail(), ipAddress);

        return ResponseEntity.ok(ForgotPasswordResponseDTO.success());
    }

    /**
     * Valida um token de recuperação de senha.
     *
     * @param token Token de recuperação
     * @return Resultado da validação com email mascarado se válido
     */
    @GetMapping("/reset/{token}")
    public ResponseEntity<ValidateTokenResponseDTO> validateToken(
            @PathVariable String token) {

        log.info("Token validation requested");

        ValidateTokenResponseDTO response = passwordResetService.validateToken(token);

        if (response.isValid()) {
            log.info("Token validated successfully");
        } else {
            log.warn("Token validation failed: {}", response.getError());
        }

        return ResponseEntity.ok(response);
    }

    /**
     * Redefine a senha do usuário.
     *
     * @param request DTO com token e nova senha
     * @return Resultado da operação
     */
    @PostMapping("/reset")
    public ResponseEntity<ResetPasswordResponseDTO> resetPassword(
            @Valid @RequestBody ResetPasswordRequestDTO request) {

        log.info("Password reset requested");

        passwordResetService.resetPassword(request.getToken(), request.getPassword());

        log.info("Password reset completed successfully");
        return ResponseEntity.ok(ResetPasswordResponseDTO.success());
    }

    /**
     * Mascara email para logging seguro.
     */
    private String maskEmail(String email) {
        if (email == null || !email.contains("@")) {
            return "***";
        }
        int atIndex = email.indexOf("@");
        if (atIndex <= 1) {
            return "***" + email.substring(atIndex);
        }
        return email.charAt(0) + "***" + email.substring(atIndex);
    }
}
