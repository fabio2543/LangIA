package com.langia.backend.service;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import com.langia.backend.dto.PermissionDTO;
import com.langia.backend.dto.ProfileWithPermissionsDTO;
import com.langia.backend.model.LessonContext;
import com.langia.backend.model.Functionality;
import com.langia.backend.model.Profile;
import com.langia.backend.model.ProfileFunctionality;
import com.langia.backend.model.StudentContext;
import com.langia.backend.model.User;
import com.langia.backend.model.UserProfile;
import com.langia.backend.repository.ProfileFunctionalityRepository;
import com.langia.backend.repository.ProfileRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class PermissionService {

    private final ProfileRepository profileRepository;
    private final ProfileFunctionalityRepository profileFunctionalityRepository;

    private final ConcurrentMap<UserProfile, PermissionCacheEntry> permissionCache = new ConcurrentHashMap<>();

    @Transactional(readOnly = true)
    public Set<String> getPermissions(UserProfile profile) {
        return getOrLoadEntry(profile).permissions();
    }

    @Transactional(readOnly = true)
    public boolean hasPermission(UserProfile profile, String permission) {
        if (!StringUtils.hasText(permission)) {
            return false;
        }
        return getPermissions(profile).contains(permission.trim());
    }

    @Transactional(readOnly = true)
    public boolean hasAllPermissions(UserProfile profile, String... permissions) {
        if (permissions == null || permissions.length == 0) {
            return true;
        }
        Set<String> granted = getPermissions(profile);
        return Arrays.stream(permissions)
                .filter(StringUtils::hasText)
                .map(String::trim)
                .allMatch(granted::contains);
    }

    @Transactional(readOnly = true)
    public boolean hasAnyPermission(UserProfile profile, String... permissions) {
        if (permissions == null || permissions.length == 0) {
            return true;
        }
        Set<String> granted = getPermissions(profile);
        return Arrays.stream(permissions)
                .filter(StringUtils::hasText)
                .map(String::trim)
                .anyMatch(granted::contains);
    }

    @Transactional(readOnly = true)
    public ProfileWithPermissionsDTO getPerfilComPermissoes(UserProfile profile) {
        Profile profileEntity = getProfileEntity(profile);
        PermissionCacheEntry entry = getOrLoadEntry(profile);
        return ProfileWithPermissionsDTO.from(profileEntity, entry.detailedPermissions());
    }

    @Transactional(readOnly = true)
    public void refreshPermissions(UserProfile profile) {
        if (profile == null) {
            permissionCache.clear();
            log.info("Permission cache fully cleared");
            return;
        }
        permissionCache.compute(profile, (key, oldValue) -> loadPermissions(key));
        log.info("Permission cache refreshed for profile {}", profile);
    }

    public void evictPermissions(UserProfile profile) {
        if (profile == null) {
            permissionCache.clear();
            log.info("Permission cache fully evicted");
            return;
        }
        permissionCache.remove(profile);
        log.info("Permission cache entry evicted for profile {}", profile);
    }

    public boolean canEditAula(User user, LessonContext lesson) {
        if (user == null || lesson == null || user.getId() == null) {
            return false;
        }
        return isAdmin(user) || lesson.isOwnedBy(user.getId());
    }

    public boolean canViewAluno(User user, StudentContext student) {
        if (user == null || student == null || user.getId() == null) {
            return false;
        }
        return isAdmin(user) || student.isStudentOfTeacher(user.getId());
    }

    private PermissionCacheEntry getOrLoadEntry(UserProfile profile) {
        UserProfile validated = Objects.requireNonNull(profile, "Profile must not be null");
        return permissionCache.computeIfAbsent(validated, this::loadPermissions);
    }

    private PermissionCacheEntry loadPermissions(UserProfile profile) {
        log.debug("Loading permissions for profile {} (including inheritance)", profile);
        Map<String, PermissionDTO> permissionByCode = new LinkedHashMap<>();

        for (UserProfile candidate : getProfilesInHierarchy(profile)) {
            Profile resolvedProfile = getProfileEntity(candidate);
            List<ProfileFunctionality> links = profileFunctionalityRepository.findByProfileId(resolvedProfile.getId());
            for (ProfileFunctionality link : links) {
                Functionality functionality = link.getFunctionality();
                if (functionality == null || !StringUtils.hasText(functionality.getCode())) {
                    continue;
                }
                permissionByCode.putIfAbsent(functionality.getCode(), PermissionDTO.fromEntity(functionality));
            }
        }

        Set<String> codes = Collections.unmodifiableSet(new LinkedHashSet<>(permissionByCode.keySet()));
        List<PermissionDTO> details = Collections
                .unmodifiableList(new ArrayList<>(permissionByCode.values()));
        log.debug("Resolved {} permissions for {}", codes.size(), profile);
        return new PermissionCacheEntry(codes, details);
    }

    private List<UserProfile> getProfilesInHierarchy(UserProfile profile) {
        return Arrays.stream(UserProfile.values())
                .filter(candidate -> candidate.getHierarchyLevel() <= profile.getHierarchyLevel())
                .sorted(Comparator.comparingInt(UserProfile::getHierarchyLevel))
                .collect(Collectors.toUnmodifiableList());
    }

    private Profile getProfileEntity(UserProfile profile) {
        return profileRepository.findByCode(profile)
                .orElseThrow(() -> new IllegalStateException("Profile not configured: " + profile));
    }

    private boolean isAdmin(User user) {
        return user != null && user.getProfile() == UserProfile.ADMIN;
    }

    private record PermissionCacheEntry(Set<String> permissions, List<PermissionDTO> detailedPermissions) {
    }
}

