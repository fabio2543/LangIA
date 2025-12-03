package com.langia.backend.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

import com.langia.backend.dto.EmailVerificationResponseDTO;
import com.langia.backend.dto.ErrorResponse;
import com.langia.backend.dto.PendingVerificationResponseDTO;
import com.langia.backend.dto.RateLimitExceededResponseDTO;
import com.langia.backend.dto.ResendVerificationResponseDTO;
import com.langia.backend.dto.ResetPasswordResponseDTO;

import lombok.extern.slf4j.Slf4j;

/**
 * Handler global de exceções para toda a aplicação.
 * Centraliza o tratamento de erros e padroniza as respostas HTTP.
 */
@ControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

    // ========== Exceções de Autenticação ==========

    /**
     * Trata exceções de credenciais inválidas (login).
     */
    @ExceptionHandler(InvalidCredentialsException.class)
    public ResponseEntity<ErrorResponse> handleInvalidCredentials(InvalidCredentialsException ex) {
        log.warn("Credenciais inválidas: {}", ex.getMessage());
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body(new ErrorResponse("Invalid credentials"));
    }

    /**
     * Trata exceções de token ausente ou mal formatado.
     */
    @ExceptionHandler(MissingTokenException.class)
    public ResponseEntity<ErrorResponse> handleMissingToken(MissingTokenException ex) {
        log.warn("Token ausente ou inválido: {}", ex.getMessage());
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body(new ErrorResponse(ex.getMessage()));
    }

    /**
     * Trata exceções de sessão inválida ou expirada.
     */
    @ExceptionHandler(InvalidSessionException.class)
    public ResponseEntity<ErrorResponse> handleInvalidSession(InvalidSessionException ex) {
        log.warn("Sessão inválida: {}", ex.getMessage());
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body(new ErrorResponse(ex.getMessage()));
    }

    // ========== Exceções de Registro de Usuário ==========

    /**
     * Trata exceções de email já cadastrado.
     */
    @ExceptionHandler(EmailAlreadyExistsException.class)
    public ResponseEntity<ErrorResponse> handleEmailAlreadyExists(EmailAlreadyExistsException ex) {
        log.warn("Email já cadastrado: {}", ex.getMessage());
        return ResponseEntity.status(HttpStatus.CONFLICT)
                .body(new ErrorResponse(ex.getMessage()));
    }

    /**
     * Trata exceções de CPF já cadastrado.
     */
    @ExceptionHandler(CpfAlreadyExistsException.class)
    public ResponseEntity<ErrorResponse> handleCpfAlreadyExists(CpfAlreadyExistsException ex) {
        log.warn("CPF já cadastrado: {}", ex.getMessage());
        return ResponseEntity.status(HttpStatus.CONFLICT)
                .body(new ErrorResponse("CPF já cadastrado no sistema"));
    }

    /**
     * Trata exceções de telefone já cadastrado.
     */
    @ExceptionHandler(PhoneAlreadyExistsException.class)
    public ResponseEntity<ErrorResponse> handlePhoneAlreadyExists(PhoneAlreadyExistsException ex) {
        log.warn("Telefone já cadastrado: {}", ex.getMessage());
        return ResponseEntity.status(HttpStatus.CONFLICT)
                .body(new ErrorResponse("Telefone já cadastrado no sistema"));
    }

    // ========== Exceções de Recuperação de Senha ==========

    /**
     * Trata exceções de rate limit excedido.
     * Retorna 429 Too Many Requests com header Retry-After.
     */
    @ExceptionHandler(RateLimitExceededException.class)
    public ResponseEntity<RateLimitExceededResponseDTO> handleRateLimitExceeded(
            RateLimitExceededException ex) {
        log.warn("Rate limit exceeded: {} seconds until reset", ex.getRetryAfterSeconds());
        return ResponseEntity.status(HttpStatus.TOO_MANY_REQUESTS)
                .header("Retry-After", String.valueOf(ex.getRetryAfterSeconds()))
                .body(RateLimitExceededResponseDTO.create(ex.getRetryAfterSeconds()));
    }

    /**
     * Trata exceções de token de reset inválido ou expirado.
     */
    @ExceptionHandler(InvalidResetTokenException.class)
    public ResponseEntity<ResetPasswordResponseDTO> handleInvalidResetToken(
            InvalidResetTokenException ex) {
        log.warn("Invalid reset token: {}", ex.getMessage());
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(ResetPasswordResponseDTO.tokenInvalid());
    }

    /**
     * Trata exceções de senha recentemente utilizada.
     */
    @ExceptionHandler(PasswordRecentlyUsedException.class)
    public ResponseEntity<ResetPasswordResponseDTO> handlePasswordRecentlyUsed(
            PasswordRecentlyUsedException ex) {
        log.warn("Password recently used: {}", ex.getMessage());
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(ResetPasswordResponseDTO.passwordRecentlyUsed());
    }

    /**
     * Trata exceções de validação de complexidade de senha.
     */
    @ExceptionHandler(PasswordValidationException.class)
    public ResponseEntity<ResetPasswordResponseDTO> handlePasswordValidation(
            PasswordValidationException ex) {
        log.warn("Password validation failed: {}", ex.getErrors());
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(ResetPasswordResponseDTO.passwordValidationError(ex.getErrors()));
    }

    // ========== Excecoes de Verificacao de E-mail ==========

    /**
     * Trata excecao de e-mail nao verificado (tentativa de login).
     * Retorna 403 Forbidden com dados para redirecionar o usuario.
     */
    @ExceptionHandler(EmailNotVerifiedException.class)
    public ResponseEntity<PendingVerificationResponseDTO> handleEmailNotVerified(
            EmailNotVerifiedException ex) {
        log.warn("Login attempt with unverified email for user: {}", ex.getUserId());
        return ResponseEntity.status(HttpStatus.FORBIDDEN)
                .body(PendingVerificationResponseDTO.builder()
                    .userId(ex.getUserId())
                    .maskedEmail(ex.getMaskedEmail())
                    .emailVerificationRequired(true)
                    .message("Por favor, verifique seu e-mail antes de fazer login.")
                    .build());
    }

    /**
     * Trata excecao de token de verificacao invalido.
     */
    @ExceptionHandler(InvalidVerificationTokenException.class)
    public ResponseEntity<EmailVerificationResponseDTO> handleInvalidVerificationToken(
            InvalidVerificationTokenException ex) {
        log.warn("Invalid verification token: {}", ex.getErrorCode());

        EmailVerificationResponseDTO response;
        switch (ex.getErrorCode()) {
            case "TOKEN_EXPIRED":
                response = EmailVerificationResponseDTO.tokenExpired();
                break;
            case "TOKEN_USED":
                response = EmailVerificationResponseDTO.tokenUsed();
                break;
            default:
                response = EmailVerificationResponseDTO.tokenInvalid();
        }

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
    }

    /**
     * Trata excecao de rate limit de verificacao de e-mail.
     */
    @ExceptionHandler(EmailVerificationRateLimitException.class)
    public ResponseEntity<ResendVerificationResponseDTO> handleEmailVerificationRateLimit(
            EmailVerificationRateLimitException ex) {
        log.warn("Email verification rate limit exceeded: {} seconds until reset",
            ex.getRetryAfterSeconds());
        return ResponseEntity.status(HttpStatus.TOO_MANY_REQUESTS)
                .header("Retry-After", String.valueOf(ex.getRetryAfterSeconds()))
                .body(ResendVerificationResponseDTO.rateLimited(ex.getRetryAfterSeconds()));
    }

    // ========== Exceções de Trilhas ==========

    /**
     * Trata exceção de trilha não encontrada.
     */
    @ExceptionHandler(TrailNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleTrailNotFound(TrailNotFoundException ex) {
        log.warn("Trilha não encontrada: {}", ex.getMessage());
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(new ErrorResponse(ex.getMessage()));
    }

    /**
     * Trata exceção de módulo não encontrado.
     */
    @ExceptionHandler(ModuleNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleModuleNotFound(ModuleNotFoundException ex) {
        log.warn("Módulo não encontrado: {}", ex.getMessage());
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(new ErrorResponse(ex.getMessage()));
    }

    /**
     * Trata exceção de lição não encontrada.
     */
    @ExceptionHandler(LessonNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleLessonNotFound(LessonNotFoundException ex) {
        log.warn("Lição não encontrada: {}", ex.getMessage());
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(new ErrorResponse(ex.getMessage()));
    }

    /**
     * Trata exceção de limite de trilhas excedido.
     */
    @ExceptionHandler(TrailLimitExceededException.class)
    public ResponseEntity<ErrorResponse> handleTrailLimitExceeded(TrailLimitExceededException ex) {
        log.warn("Limite de trilhas excedido: {}", ex.getMessage());
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(new ErrorResponse(ex.getMessage()));
    }

    /**
     * Trata exceção de erro na geração de trilha.
     */
    @ExceptionHandler(TrailGenerationException.class)
    public ResponseEntity<ErrorResponse> handleTrailGenerationError(TrailGenerationException ex) {
        log.error("Erro na geração de trilha: {}", ex.getMessage(), ex);
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(new ErrorResponse("Erro ao gerar trilha. Tente novamente mais tarde."));
    }

    // ========== Exceções de Negócio ==========

    /**
     * Trata exceções de recurso não encontrado.
     * Retorna 404 Not Found.
     */
    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleResourceNotFound(ResourceNotFoundException ex) {
        log.warn("Resource not found: {}", ex.getMessage());
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(new ErrorResponse(ex.getMessage()));
    }

    /**
     * Trata exceções de regras de negócio.
     * Retorna 400 Bad Request.
     */
    @ExceptionHandler(BusinessException.class)
    public ResponseEntity<ErrorResponse> handleBusinessException(BusinessException ex) {
        log.warn("Business rule violation: {}", ex.getMessage());
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(new ErrorResponse(ex.getMessage()));
    }

    // ========== Exceções de Validação ==========

    /**
     * Trata exceções de validação de DTOs (@Valid).
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleValidationErrors(MethodArgumentNotValidException ex) {
        String message = ex.getBindingResult().getFieldErrors().stream()
                .map(error -> error.getField() + ": " + error.getDefaultMessage())
                .findFirst()
                .orElse("Erro de validação");
        log.warn("Erro de validação: {}", message);
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(new ErrorResponse(message));
    }

    // ========== Exceção Genérica (Fallback) ==========

    /**
     * Trata qualquer exceção não mapeada (fallback).
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleGenericException(Exception ex) {
        log.error("Erro inesperado: {}", ex.getMessage(), ex);
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(new ErrorResponse("An unexpected error occurred"));
    }
}
