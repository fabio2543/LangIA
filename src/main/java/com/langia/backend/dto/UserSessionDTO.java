package com.langia.backend.dto;

import java.io.Serializable;
import java.util.Set;
import java.util.UUID;

import com.langia.backend.model.UserProfile;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Represents the information stored in Redis for an authenticated user session.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserSessionDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    private UUID userId;
    private String name;
    private String email;
    private UserProfile profile;
    private Set<String> permissions;
    /**
     * Epoch milliseconds indicating when the token linked to this session expires.
     */
    private Long expiresAt;
}


