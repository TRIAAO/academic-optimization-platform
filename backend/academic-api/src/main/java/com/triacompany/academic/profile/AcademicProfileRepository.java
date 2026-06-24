package com.triacompany.academic.profile;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface AcademicProfileRepository extends JpaRepository<AcademicProfile, UUID> {

    Optional<AcademicProfile> findByResearcherId(UUID researcherId);

    boolean existsByResearcherId(UUID researcherId);
}