package com.langia.backend.dto;

import java.util.Set;
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
public class LoginResponseDTO {

    private String token;
    private UUID userId;
    private String name;
    private String email;
    private UserProfile profile;
    private Set<String> permissions;
    private Long expiresIn;

    /**
     * Creates a LoginResponseDTO from a User entity and token information.
     *
     * @param user        the authenticated user
     * @param token       the JWT token
     * @param permissions the set of permissions for the user's profile
     * @param expiresIn   token expiration time in milliseconds
     * @return LoginResponseDTO with all user and token information
     */
    public static LoginResponseDTO fromUser(User user, String token, Set<String> permissions, Long expiresIn) {
        return LoginResponseDTO.builder()
                .token(token)
                .userId(user.getId())
                .name(user.getName())
                .email(user.getEmail())
                .profile(user.getProfile())
                .permissions(permissions)
                .expiresIn(expiresIn)
                .build();
    }
}
