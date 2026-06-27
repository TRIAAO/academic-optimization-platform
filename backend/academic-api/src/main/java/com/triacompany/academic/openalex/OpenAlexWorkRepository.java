package com.triacompany.academic.openalex;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface OpenAlexWorkRepository extends JpaRepository<OpenAlexWork, UUID> {

    List<OpenAlexWork> findByResearcherIdOrderByPublicationYearDescTitleAsc(UUID researcherId);

    boolean existsByResearcherIdAndOpenAlexId(UUID researcherId, String openAlexId);
}