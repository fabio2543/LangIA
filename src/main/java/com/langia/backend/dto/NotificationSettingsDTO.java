package com.langia.backend.dto;

import java.util.Map;

import com.langia.backend.model.NotificationCategory;
import com.langia.backend.model.NotificationChannel;
import com.langia.backend.model.ReminderFrequency;

import jakarta.validation.constraints.Pattern;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO for notification settings (GET and PUT).
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class NotificationSettingsDTO {

    private Map<NotificationChannel, Boolean> activeChannels;

    private Map<NotificationCategory, CategoryPreference> categoryPreferences;

    private ReminderFrequency reminderFrequency;

    @Pattern(regexp = "^([01]\\d|2[0-3]):[0-5]\\d$", message = "Format HH:mm")
    private String preferredTimeStart;

    @Pattern(regexp = "^([01]\\d|2[0-3]):[0-5]\\d$", message = "Format HH:mm")
    private String preferredTimeEnd;

    @Pattern(regexp = "^([01]\\d|2[0-3]):[0-5]\\d$", message = "Format HH:mm")
    private String quietModeStart;

    @Pattern(regexp = "^([01]\\d|2[0-3]):[0-5]\\d$", message = "Format HH:mm")
    private String quietModeEnd;
}
