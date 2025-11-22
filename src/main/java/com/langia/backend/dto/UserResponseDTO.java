package com.langia.backend.dto;

import java.time.LocalDateTime;
import java.util.UUID;

import com.langia.backend.model.User;
import com.langia.backend.model.UserProfile;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserResponseDTO {

    private UUID id;
    private String name;
    private String email;
    private UserProfile profile;
    private String phone;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    /**
     * Converts a User entity to UserResponseDTO, excluding sensitive data.
     *
     * @param user the User entity to convert
     * @return UserResponseDTO without sensitive information
     */
    public static UserResponseDTO fromUser(User user) {
        return UserResponseDTO.builder()
                .id(user.getId())
                .name(user.getName())
                .email(user.getEmail())
                .profile(user.getProfile())
                .phone(user.getPhone())
                .createdAt(user.getCreatedAt())
                .updatedAt(user.getUpdatedAt())
                .build();
    }
}
