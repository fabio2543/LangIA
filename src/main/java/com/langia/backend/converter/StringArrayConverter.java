package com.langia.backend.converter;

import java.sql.Array;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

/**
 * JPA converter for PostgreSQL TEXT[] arrays to Java List<String>.
 */
@Converter
public class StringArrayConverter implements AttributeConverter<List<String>, Array> {

    @Override
    public Array convertToDatabaseColumn(List<String> attribute) {
        // Hibernate handles this via dialect; return null for empty/null
        return null;
    }

    @Override
    public List<String> convertToEntityAttribute(Array dbData) {
        if (dbData == null) {
            return new ArrayList<>();
        }
        try {
            String[] array = (String[]) dbData.getArray();
            return array == null ? new ArrayList<>() : new ArrayList<>(Arrays.asList(array));
        } catch (SQLException e) {
            return new ArrayList<>();
        }
    }
}
