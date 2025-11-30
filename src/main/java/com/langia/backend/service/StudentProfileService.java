package com.langia.backend.service;

import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.langia.backend.dto.CategoryPreference;
import com.langia.backend.dto.LearningPreferencesDTO;
import com.langia.backend.dto.NotificationSettingsDTO;
import com.langia.backend.dto.SkillAssessmentDTO;
import com.langia.backend.dto.SkillAssessmentResponseDTO;
import com.langia.backend.dto.UpdatePersonalDataDTO;
import com.langia.backend.dto.UserProfileDetailsDTO;
import com.langia.backend.exception.UserNotFoundException;
import com.langia.backend.model.NotificationCategory;
import com.langia.backend.model.NotificationChannel;
import com.langia.backend.model.NotificationSettingsEntity;
import com.langia.backend.model.ReminderFrequency;
import com.langia.backend.model.StudentLearningPreferences;
import com.langia.backend.model.StudentSkillAssessment;
import com.langia.backend.model.User;
import com.langia.backend.model.UserProfileDetails;
import com.langia.backend.repository.NotificationSettingsRepository;
import com.langia.backend.repository.StudentLearningPreferencesRepository;
import com.langia.backend.repository.StudentSkillAssessmentRepository;
import com.langia.backend.repository.UserProfileDetailsRepository;
import com.langia.backend.repository.UserRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * Service for managing student profile data.
 * Note: Language enrollments are now managed separately via StudentLanguageService.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class StudentProfileService {

    private final UserRepository userRepository;
    private final UserProfileDetailsRepository profileDetailsRepository;
    private final StudentLearningPreferencesRepository preferencesRepository;
    private final StudentSkillAssessmentRepository assessmentRepository;
    private final NotificationSettingsRepository notificationRepository;

    private static final DateTimeFormatter TIME_FORMATTER = DateTimeFormatter.ofPattern("HH:mm");

    // ========== Profile Details ==========

    @Transactional(readOnly = true)
    public UserProfileDetailsDTO getProfileDetails(UUID userId) {
        User user = findUserOrThrow(userId);
        UserProfileDetails details = profileDetailsRepository.findByUserId(userId).orElse(null);
        return mapToProfileDetailsDTO(user, details);
    }

    @Transactional
    public UserProfileDetailsDTO updateProfileDetails(UUID userId, UpdatePersonalDataDTO dto) {
        User user = findUserOrThrow(userId);

        // Update User fields
        if (dto.getFullName() != null) {
            user.setName(dto.getFullName());
        }
        if (dto.getWhatsappPhone() != null) {
            user.setPhone(dto.getWhatsappPhone());
        }
        userRepository.save(user);

        // Update or create UserProfileDetails
        UserProfileDetails details = profileDetailsRepository.findByUserId(userId)
                .orElse(UserProfileDetails.builder().user(user).build());

        if (dto.getNativeLanguage() != null) {
            details.setNativeLanguage(dto.getNativeLanguage());
        }
        if (dto.getTimezone() != null) {
            details.setTimezone(dto.getTimezone());
        }
        if (dto.getBirthDate() != null) {
            details.setBirthDate(dto.getBirthDate());
        }
        if (dto.getBio() != null) {
            details.setBio(dto.getBio());
        }

        profileDetailsRepository.save(details);
        log.info("Profile details updated for user {}", userId);
        return mapToProfileDetailsDTO(user, details);
    }

    // ========== Learning Preferences ==========

    @Transactional(readOnly = true)
    public LearningPreferencesDTO getLearningPreferences(UUID userId) {
        findUserOrThrow(userId);
        StudentLearningPreferences prefs = preferencesRepository.findByUserId(userId).orElse(null);
        return mapToLearningPreferencesDTO(prefs);
    }

    @Transactional
    public LearningPreferencesDTO updateLearningPreferences(UUID userId, LearningPreferencesDTO dto) {
        User user = findUserOrThrow(userId);

        StudentLearningPreferences prefs = preferencesRepository.findByUserId(userId)
                .orElse(StudentLearningPreferences.builder().user(user).build());

        // Map fields from DTO (all are now strings/lists of strings)
        prefs.setDailyTimeAvailable(dto.getDailyTimeAvailable());
        prefs.setPreferredDays(dto.getPreferredDays() != null ? dto.getPreferredDays() : new ArrayList<>());
        prefs.setPreferredTimes(dto.getPreferredTimes() != null ? dto.getPreferredTimes() : new ArrayList<>());
        prefs.setWeeklyHoursGoal(dto.getWeeklyHoursGoal());
        prefs.setTopicsOfInterest(dto.getTopicsOfInterest() != null ? dto.getTopicsOfInterest() : new ArrayList<>());
        prefs.setCustomTopics(dto.getCustomTopics() != null ? dto.getCustomTopics() : new ArrayList<>());
        prefs.setPreferredFormats(dto.getPreferredFormats() != null ? dto.getPreferredFormats() : new ArrayList<>());
        prefs.setFormatRanking(dto.getFormatRanking() != null ? dto.getFormatRanking() : new ArrayList<>());
        prefs.setPrimaryObjective(dto.getPrimaryObjective());
        prefs.setObjectiveDescription(dto.getObjectiveDescription());
        prefs.setObjectiveDeadline(dto.getObjectiveDeadline());

        preferencesRepository.save(prefs);
        log.info("Learning preferences updated for user {}", userId);
        return mapToLearningPreferencesDTO(prefs);
    }

    // ========== Skill Assessment ==========

    @Transactional(readOnly = true)
    public List<SkillAssessmentResponseDTO> getSkillAssessments(UUID userId) {
        findUserOrThrow(userId);
        return assessmentRepository.findByUserIdOrderByAssessedAtDesc(userId).stream()
                .map(this::mapToSkillAssessmentResponseDTO)
                .toList();
    }

    @Transactional
    public SkillAssessmentResponseDTO createSkillAssessment(UUID userId, SkillAssessmentDTO dto) {
        User user = findUserOrThrow(userId);

        StudentSkillAssessment assessment = StudentSkillAssessment.builder()
                .user(user)
                .language(dto.getLanguage())
                .listeningDifficulty(dto.getListeningDifficulty())
                .speakingDifficulty(dto.getSpeakingDifficulty())
                .readingDifficulty(dto.getReadingDifficulty())
                .writingDifficulty(dto.getWritingDifficulty())
                .listeningDetails(dto.getListeningDetails() != null ? dto.getListeningDetails() : new ArrayList<>())
                .speakingDetails(dto.getSpeakingDetails() != null ? dto.getSpeakingDetails() : new ArrayList<>())
                .readingDetails(dto.getReadingDetails() != null ? dto.getReadingDetails() : new ArrayList<>())
                .writingDetails(dto.getWritingDetails() != null ? dto.getWritingDetails() : new ArrayList<>())
                .selfCefrLevel(dto.getSelfCefrLevel())
                .build();

        assessmentRepository.save(assessment);
        log.info("Skill assessment created for user {} in language {}", userId, dto.getLanguage());
        return mapToSkillAssessmentResponseDTO(assessment);
    }

    // ========== Notification Settings ==========

    @Transactional(readOnly = true)
    public NotificationSettingsDTO getNotificationSettings(UUID userId) {
        findUserOrThrow(userId);
        NotificationSettingsEntity settings = notificationRepository.findByUserId(userId).orElse(null);
        return mapToNotificationSettingsDTO(settings);
    }

    @Transactional
    public NotificationSettingsDTO updateNotificationSettings(UUID userId, NotificationSettingsDTO dto) {
        User user = findUserOrThrow(userId);

        NotificationSettingsEntity settings = notificationRepository.findByUserId(userId)
                .orElse(NotificationSettingsEntity.builder().user(user).build());

        // Convert channel map
        if (dto.getActiveChannels() != null) {
            Map<String, Boolean> channelMap = dto.getActiveChannels().entrySet().stream()
                    .collect(Collectors.toMap(e -> e.getKey().name(), Map.Entry::getValue));
            settings.setActiveChannels(channelMap);
        }

        // Convert category preferences to generic map for JSONB
        if (dto.getCategoryPreferences() != null) {
            Map<String, Object> categoryMap = new HashMap<>();
            dto.getCategoryPreferences().forEach((key, value) -> {
                Map<String, Object> prefMap = new HashMap<>();
                prefMap.put("active", value.isActive());
                prefMap.put("channels", value.getChannels().stream().map(Enum::name).toList());
                categoryMap.put(key.name(), prefMap);
            });
            settings.setCategoryPreferences(categoryMap);
        }

        settings.setReminderFrequency(dto.getReminderFrequency());
        settings.setPreferredTimeStart(parseTime(dto.getPreferredTimeStart()));
        settings.setPreferredTimeEnd(parseTime(dto.getPreferredTimeEnd()));
        settings.setQuietModeStart(parseTime(dto.getQuietModeStart()));
        settings.setQuietModeEnd(parseTime(dto.getQuietModeEnd()));

        notificationRepository.save(settings);
        log.info("Notification settings updated for user {}", userId);
        return mapToNotificationSettingsDTO(settings);
    }

    // ========== Helper Methods ==========

    private User findUserOrThrow(UUID userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException("User not found: " + userId));
    }

    private LocalTime parseTime(String time) {
        if (time == null || time.isBlank()) {
            return null;
        }
        return LocalTime.parse(time, TIME_FORMATTER);
    }

    private String formatTime(LocalTime time) {
        return time == null ? null : time.format(TIME_FORMATTER);
    }

    // ========== Mappers ==========

    private UserProfileDetailsDTO mapToProfileDetailsDTO(User user, UserProfileDetails details) {
        return UserProfileDetailsDTO.builder()
                .id(user.getId())
                .fullName(user.getName())
                .email(user.getEmail())
                .whatsappPhone(user.getPhone())
                .nativeLanguage(details != null ? details.getNativeLanguage() : null)
                .timezone(details != null ? details.getTimezone() : "America/Sao_Paulo")
                .birthDate(details != null ? details.getBirthDate() : null)
                .bio(details != null ? details.getBio() : null)
                .build();
    }

    private LearningPreferencesDTO mapToLearningPreferencesDTO(StudentLearningPreferences prefs) {
        if (prefs == null) {
            return LearningPreferencesDTO.builder()
                    .preferredDays(new ArrayList<>())
                    .preferredTimes(new ArrayList<>())
                    .topicsOfInterest(new ArrayList<>())
                    .customTopics(new ArrayList<>())
                    .preferredFormats(new ArrayList<>())
                    .formatRanking(new ArrayList<>())
                    .build();
        }

        return LearningPreferencesDTO.builder()
                .dailyTimeAvailable(prefs.getDailyTimeAvailable())
                .preferredDays(prefs.getPreferredDays() != null ? prefs.getPreferredDays() : new ArrayList<>())
                .preferredTimes(prefs.getPreferredTimes() != null ? prefs.getPreferredTimes() : new ArrayList<>())
                .weeklyHoursGoal(prefs.getWeeklyHoursGoal())
                .topicsOfInterest(prefs.getTopicsOfInterest() != null ? prefs.getTopicsOfInterest() : new ArrayList<>())
                .customTopics(prefs.getCustomTopics() != null ? prefs.getCustomTopics() : new ArrayList<>())
                .preferredFormats(prefs.getPreferredFormats() != null ? prefs.getPreferredFormats() : new ArrayList<>())
                .formatRanking(prefs.getFormatRanking() != null ? prefs.getFormatRanking() : new ArrayList<>())
                .primaryObjective(prefs.getPrimaryObjective())
                .objectiveDescription(prefs.getObjectiveDescription())
                .objectiveDeadline(prefs.getObjectiveDeadline())
                .build();
    }

    private SkillAssessmentResponseDTO mapToSkillAssessmentResponseDTO(StudentSkillAssessment assessment) {
        return SkillAssessmentResponseDTO.builder()
                .id(assessment.getId())
                .language(assessment.getLanguage())
                .listeningDifficulty(assessment.getListeningDifficulty())
                .speakingDifficulty(assessment.getSpeakingDifficulty())
                .readingDifficulty(assessment.getReadingDifficulty())
                .writingDifficulty(assessment.getWritingDifficulty())
                .listeningDetails(assessment.getListeningDetails())
                .speakingDetails(assessment.getSpeakingDetails())
                .readingDetails(assessment.getReadingDetails())
                .writingDetails(assessment.getWritingDetails())
                .selfCefrLevel(assessment.getSelfCefrLevel())
                .assessedAt(assessment.getAssessedAt())
                .build();
    }

    @SuppressWarnings("unchecked")
    private NotificationSettingsDTO mapToNotificationSettingsDTO(NotificationSettingsEntity settings) {
        if (settings == null) {
            // Return defaults
            Map<NotificationChannel, Boolean> defaultChannels = new HashMap<>();
            defaultChannels.put(NotificationChannel.PUSH, true);
            defaultChannels.put(NotificationChannel.EMAIL, true);
            defaultChannels.put(NotificationChannel.WHATSAPP, false);

            return NotificationSettingsDTO.builder()
                    .activeChannels(defaultChannels)
                    .categoryPreferences(new HashMap<>())
                    .reminderFrequency(ReminderFrequency.DAILY)
                    .build();
        }

        // Convert channel map
        Map<NotificationChannel, Boolean> channelMap = new HashMap<>();
        if (settings.getActiveChannels() != null) {
            settings.getActiveChannels().forEach((k, v) -> {
                try {
                    channelMap.put(NotificationChannel.valueOf(k), v);
                } catch (Exception e) {
                    // Skip invalid
                }
            });
        }

        // Convert category preferences
        Map<NotificationCategory, CategoryPreference> categoryMap = new HashMap<>();
        if (settings.getCategoryPreferences() != null) {
            settings.getCategoryPreferences().forEach((k, v) -> {
                try {
                    NotificationCategory cat = NotificationCategory.valueOf(k);
                    if (v instanceof Map) {
                        Map<String, Object> prefMap = (Map<String, Object>) v;
                        boolean active = Boolean.TRUE.equals(prefMap.get("active"));
                        List<NotificationChannel> channels = new ArrayList<>();
                        if (prefMap.get("channels") instanceof List) {
                            ((List<String>) prefMap.get("channels")).forEach(ch -> {
                                try {
                                    channels.add(NotificationChannel.valueOf(ch));
                                } catch (Exception e) {
                                    // Skip invalid
                                }
                            });
                        }
                        categoryMap.put(cat, new CategoryPreference(active, channels));
                    }
                } catch (Exception e) {
                    // Skip invalid
                }
            });
        }

        return NotificationSettingsDTO.builder()
                .activeChannels(channelMap)
                .categoryPreferences(categoryMap)
                .reminderFrequency(settings.getReminderFrequency())
                .preferredTimeStart(formatTime(settings.getPreferredTimeStart()))
                .preferredTimeEnd(formatTime(settings.getPreferredTimeEnd()))
                .quietModeStart(formatTime(settings.getQuietModeStart()))
                .quietModeEnd(formatTime(settings.getQuietModeEnd()))
                .build();
    }
}
