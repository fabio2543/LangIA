package com.langia.backend.dto;

/**
 * DTO para respostas de validação de sessão.
 */
public record SessionValidationResponse(boolean valid, SessionData session) {}
