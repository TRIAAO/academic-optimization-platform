package com.triacompany.academic.editorial;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface EditorialDecisionRepository extends JpaRepository<EditorialDecision, UUID> {

    Optional<EditorialDecision> findByResearcherIdAndOpenAlexWorkId(UUID researcherId, UUID openAlexWorkId);
}
