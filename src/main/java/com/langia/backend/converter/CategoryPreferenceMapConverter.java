package com.langia.backend.converter;

import java.util.HashMap;
import java.util.Map;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.langia.backend.dto.CategoryPreference;
import com.langia.backend.model.NotificationCategory;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

/**
 * JPA converter for PostgreSQL JSONB to Map<NotificationCategory, CategoryPreference>.
 */
@Converter
public class CategoryPreferenceMapConverter
        implements AttributeConverter<Map<NotificationCategory, CategoryPreference>, String> {

    private static final ObjectMapper mapper = new ObjectMapper();

    @Override
    public String convertToDatabaseColumn(Map<NotificationCategory, CategoryPreference> attribute) {
        if (attribute == null || attribute.isEmpty()) {
            return "{}";
        }
        try {
            return mapper.writeValueAsString(attribute);
        } catch (JsonProcessingException e) {
            return "{}";
        }
    }

    @Override
    public Map<NotificationCategory, CategoryPreference> convertToEntityAttribute(String dbData) {
        if (dbData == null || dbData.isBlank()) {
            return new HashMap<>();
        }
        try {
            return mapper.readValue(dbData, new TypeReference<Map<NotificationCategory, CategoryPreference>>() {});
        } catch (Exception e) {
            return new HashMap<>();
        }
    }
}
