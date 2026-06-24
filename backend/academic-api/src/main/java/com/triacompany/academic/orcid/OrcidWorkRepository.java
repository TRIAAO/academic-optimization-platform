package com.triacompany.academic.orcid;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface OrcidWorkRepository extends JpaRepository<OrcidWork, UUID> {

    List<OrcidWork> findByResearcherIdOrderByPublicationYearDescTitleAsc(UUID researcherId);

    Optional<OrcidWork> findByResearcherIdAndPutCode(UUID researcherId, String putCode);

    boolean existsByResearcherIdAndPutCode(UUID researcherId, String putCode);
}