package com.langia.backend.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.langia.backend.dto.EmailVerificationResponseDTO;
import com.langia.backend.dto.ResendVerificationRequestDTO;
import com.langia.backend.dto.ResendVerificationResponseDTO;
import com.langia.backend.service.EmailVerificationService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * Controller para operacoes de verificacao de e-mail.
 *
 * Endpoints:
 * - GET /api/auth/email/confirm/{token} - Confirma e-mail
 * - POST /api/auth/email/resend - Reenvia verificacao
 */
@RestController
@RequestMapping("/api/auth/email")
@RequiredArgsConstructor
@Slf4j
public class EmailVerificationController {

    private final EmailVerificationService verificationService;

    /**
     * Confirma e-mail do usuario.
     *
     * @param token Token de verificacao
     * @return Resultado da confirmacao
     */
    @GetMapping("/confirm/{token}")
    public ResponseEntity<EmailVerificationResponseDTO> confirmEmail(@PathVariable String token) {
        log.info("Email confirmation requested");

        EmailVerificationResponseDTO response = verificationService.confirmEmail(token);

        log.info("Email confirmed successfully");
        return ResponseEntity.ok(response);
    }

    /**
     * Reenvia e-mail de verificacao.
     *
     * @param request DTO com userId
     * @return Resultado do reenvio
     */
    @PostMapping("/resend")
    public ResponseEntity<ResendVerificationResponseDTO> resendVerification(
            @Valid @RequestBody ResendVerificationRequestDTO request) {

        log.info("Resend verification requested for user: {}", request.getUserId());

        ResendVerificationResponseDTO response = verificationService.resendVerification(request.getUserId());

        if (response.isSuccess()) {
            log.info("Verification email resent successfully");
        } else {
            log.info("Resend verification returned: {}", response.getMessage());
        }

        return ResponseEntity.ok(response);
    }
}
