package com.langia.backend.converter;

import java.util.HashMap;
import java.util.Map;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.langia.backend.model.NotificationChannel;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

/**
 * JPA converter for PostgreSQL JSONB to Map<NotificationChannel, Boolean>.
 */
@Converter
public class NotificationChannelMapConverter implements AttributeConverter<Map<NotificationChannel, Boolean>, String> {

    private static final ObjectMapper mapper = new ObjectMapper();

    @Override
    public String convertToDatabaseColumn(Map<NotificationChannel, Boolean> attribute) {
        if (attribute == null || attribute.isEmpty()) {
            return "{\"PUSH\":true,\"EMAIL\":true,\"WHATSAPP\":false}";
        }
        try {
            return mapper.writeValueAsString(attribute);
        } catch (JsonProcessingException e) {
            return "{\"PUSH\":true,\"EMAIL\":true,\"WHATSAPP\":false}";
        }
    }

    @Override
    public Map<NotificationChannel, Boolean> convertToEntityAttribute(String dbData) {
        if (dbData == null || dbData.isBlank()) {
            return getDefaultChannels();
        }
        try {
            return mapper.readValue(dbData, new TypeReference<Map<NotificationChannel, Boolean>>() {});
        } catch (Exception e) {
            return getDefaultChannels();
        }
    }

    private Map<NotificationChannel, Boolean> getDefaultChannels() {
        Map<NotificationChannel, Boolean> defaults = new HashMap<>();
        defaults.put(NotificationChannel.PUSH, true);
        defaults.put(NotificationChannel.EMAIL, true);
        defaults.put(NotificationChannel.WHATSAPP, false);
        return defaults;
    }
}
