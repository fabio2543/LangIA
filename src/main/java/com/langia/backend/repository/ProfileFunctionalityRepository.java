package com.langia.backend.repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.langia.backend.model.Functionality;
import com.langia.backend.model.Profile;
import com.langia.backend.model.ProfileFunctionality;

@Repository
public interface ProfileFunctionalityRepository extends JpaRepository<ProfileFunctionality, UUID> {

    Optional<ProfileFunctionality> findByProfileAndFunctionality(Profile profile, Functionality functionality);

    boolean existsByProfileIdAndFunctionalityId(UUID profileId, UUID functionalityId);

    @EntityGraph(attributePaths = "functionality")
    List<ProfileFunctionality> findByProfileId(UUID profileId);

    List<ProfileFunctionality> findByFunctionalityId(UUID functionalityId);
}
