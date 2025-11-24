package com.langia.backend.repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.langia.backend.model.Profile;
import com.langia.backend.model.UserProfile;

@Repository
public interface ProfileRepository extends JpaRepository<Profile, UUID> {

    Optional<Profile> findByCode(UserProfile code);

    boolean existsByCode(UserProfile code);

    List<Profile> findAllByOrderByHierarchyLevelAsc();
}


