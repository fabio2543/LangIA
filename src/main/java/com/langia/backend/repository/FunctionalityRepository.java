package com.langia.backend.repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.langia.backend.model.Functionality;

@Repository
public interface FunctionalityRepository extends JpaRepository<Functionality, UUID> {

    Optional<Functionality> findByCode(String code);

    boolean existsByCode(String code);

    List<Functionality> findByModule(Functionality.Module module);

    List<Functionality> findByActiveTrue();
}


