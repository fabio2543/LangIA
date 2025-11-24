package com.langia.backend.dto;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import com.langia.backend.model.Profile;
import com.langia.backend.model.ProfileFunctionality;
import com.langia.backend.model.UserProfile;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProfileWithPermissionsDTO {

    private UUID id;
    private UserProfile code;
    private String name;
    private String description;
    private int hierarchyLevel;
    private boolean active;
    private List<PermissionDTO> permissions;

    public static ProfileWithPermissionsDTO fromEntity(Profile profile) {
        List<PermissionDTO> permissionDTOs = profile.getFunctionalityLinks()
                .stream()
                .map(ProfileFunctionality::getFunctionality)
                .map(PermissionDTO::fromEntity)
                .collect(Collectors.toList());

        return from(profile, permissionDTOs);
    }

    public static ProfileWithPermissionsDTO from(Profile profile, List<PermissionDTO> permissions) {
        return ProfileWithPermissionsDTO.builder()
                .id(profile.getId())
                .code(profile.getCode())
                .name(profile.getName())
                .description(profile.getDescription())
                .hierarchyLevel(profile.calculateHierarchyLevel())
                .active(profile.isActive())
                .permissions(permissions == null ? List.of() : List.copyOf(permissions))
                .build();
    }
}

