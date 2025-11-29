package com.langia.backend.dto;

import java.util.List;

import com.langia.backend.model.NotificationChannel;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Preference settings for a notification category.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class CategoryPreference {
    private boolean active;
    private List<NotificationChannel> channels;
}
