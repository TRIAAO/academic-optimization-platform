package com.triacompany.academic.orcid;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface OrcidOAuthConnectionRepository extends JpaRepository<OrcidOAuthConnection, UUID> {

    Optional<OrcidOAuthConnection> findByResearcherId(UUID researcherId);

    Optional<OrcidOAuthConnection> findByResearcherIdAndRevokedAtIsNull(UUID researcherId);

    Optional<OrcidOAuthConnection> findByOrcidId(String orcidId);
}
