package com.triacompany.academic.crossref;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface CrossrefValidationRepository extends JpaRepository<CrossrefValidation, UUID> {

    Optional<CrossrefValidation> findTopByOpenAlexWorkIdOrderByValidatedAtDesc(UUID openAlexWorkId);

    List<CrossrefValidation> findByResearcherIdOrderByValidatedAtDesc(UUID researcherId);
}