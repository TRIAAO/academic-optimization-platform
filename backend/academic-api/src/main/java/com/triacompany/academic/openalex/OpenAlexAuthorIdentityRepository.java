package com.triacompany.academic.openalex;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface OpenAlexAuthorIdentityRepository extends JpaRepository<OpenAlexAuthorIdentity, UUID> {

    Optional<OpenAlexAuthorIdentity> findByResearcherId(UUID researcherId);

    Optional<OpenAlexAuthorIdentity> findByOpenAlexAuthorId(String openAlexAuthorId);
}
