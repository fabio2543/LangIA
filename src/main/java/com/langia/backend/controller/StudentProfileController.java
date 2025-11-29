package com.langia.backend.controller;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.langia.backend.dto.LearningPreferencesDTO;
import com.langia.backend.dto.NotificationSettingsDTO;
import com.langia.backend.dto.SkillAssessmentDTO;
import com.langia.backend.dto.SkillAssessmentResponseDTO;
import com.langia.backend.dto.UpdatePersonalDataDTO;
import com.langia.backend.dto.UserProfileDetailsDTO;
import com.langia.backend.dto.SessionData;
import com.langia.backend.service.StudentProfileService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * REST Controller for student profile management.
 */
@RestController
@RequestMapping("/api/profile")
@RequiredArgsConstructor
@Slf4j
public class StudentProfileController {

    private final StudentProfileService profileService;

    // ========== Personal Data ==========

    @GetMapping("/details")
    public ResponseEntity<UserProfileDetailsDTO> getProfileDetails(
            @AuthenticationPrincipal SessionData session) {
        log.info("Getting profile details for user {}", session.getUserId());
        return ResponseEntity.ok(profileService.getProfileDetails(session.getUserId()));
    }

    @PatchMapping("/details")
    public ResponseEntity<UserProfileDetailsDTO> updateProfileDetails(
            @AuthenticationPrincipal SessionData session,
            @Valid @RequestBody UpdatePersonalDataDTO dto) {
        log.info("Updating profile details for user {}", session.getUserId());
        return ResponseEntity.ok(profileService.updateProfileDetails(session.getUserId(), dto));
    }

    // ========== Learning Preferences ==========

    @GetMapping("/learning-preferences")
    public ResponseEntity<LearningPreferencesDTO> getLearningPreferences(
            @AuthenticationPrincipal SessionData session) {
        log.info("Getting learning preferences for user {}", session.getUserId());
        return ResponseEntity.ok(profileService.getLearningPreferences(session.getUserId()));
    }

    @PutMapping("/learning-preferences")
    public ResponseEntity<LearningPreferencesDTO> updateLearningPreferences(
            @AuthenticationPrincipal SessionData session,
            @Valid @RequestBody LearningPreferencesDTO dto) {
        log.info("Updating learning preferences for user {}", session.getUserId());
        return ResponseEntity.ok(profileService.updateLearningPreferences(session.getUserId(), dto));
    }

    // ========== Skill Assessment ==========

    @GetMapping("/skill-assessments")
    public ResponseEntity<List<SkillAssessmentResponseDTO>> getSkillAssessments(
            @AuthenticationPrincipal SessionData session) {
        log.info("Getting skill assessments for user {}", session.getUserId());
        return ResponseEntity.ok(profileService.getSkillAssessments(session.getUserId()));
    }

    @PostMapping("/skill-assessments")
    public ResponseEntity<SkillAssessmentResponseDTO> createSkillAssessment(
            @AuthenticationPrincipal SessionData session,
            @Valid @RequestBody SkillAssessmentDTO dto) {
        log.info("Creating skill assessment for user {} in language {}", session.getUserId(), dto.getLanguage());
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(profileService.createSkillAssessment(session.getUserId(), dto));
    }

    // ========== Notification Settings ==========

    @GetMapping("/notification-settings")
    public ResponseEntity<NotificationSettingsDTO> getNotificationSettings(
            @AuthenticationPrincipal SessionData session) {
        log.info("Getting notification settings for user {}", session.getUserId());
        return ResponseEntity.ok(profileService.getNotificationSettings(session.getUserId()));
    }

    @PutMapping("/notification-settings")
    public ResponseEntity<NotificationSettingsDTO> updateNotificationSettings(
            @AuthenticationPrincipal SessionData session,
            @Valid @RequestBody NotificationSettingsDTO dto) {
        log.info("Updating notification settings for user {}", session.getUserId());
        return ResponseEntity.ok(profileService.updateNotificationSettings(session.getUserId(), dto));
    }
}
