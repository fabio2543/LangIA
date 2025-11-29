package com.langia.backend.controller;

import java.util.Map;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.langia.backend.dto.RequestEmailChangeDTO;
import com.langia.backend.dto.VerifyEmailChangeDTO;
import com.langia.backend.dto.SessionData;
import com.langia.backend.service.EmailChangeService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * REST Controller for email change operations.
 */
@RestController
@RequestMapping("/api/profile/email")
@RequiredArgsConstructor
@Slf4j
public class EmailChangeController {

    private final EmailChangeService emailChangeService;

    @PostMapping("/change-request")
    public ResponseEntity<Map<String, String>> requestEmailChange(
            @AuthenticationPrincipal SessionData session,
            @Valid @RequestBody RequestEmailChangeDTO dto) {
        log.info("Email change requested for user {}", session.getUserId());
        emailChangeService.requestEmailChange(session.getUserId(), dto.getNewEmail());
        return ResponseEntity.ok(Map.of("message", "Verification code sent to new email"));
    }

    @PostMapping("/verify")
    public ResponseEntity<Map<String, String>> verifyEmailChange(
            @AuthenticationPrincipal SessionData session,
            @Valid @RequestBody VerifyEmailChangeDTO dto) {
        log.info("Email change verification for user {}", session.getUserId());
        emailChangeService.confirmEmailChange(session.getUserId(), dto.getCode());
        return ResponseEntity.ok(Map.of("message", "Email changed successfully"));
    }
}
