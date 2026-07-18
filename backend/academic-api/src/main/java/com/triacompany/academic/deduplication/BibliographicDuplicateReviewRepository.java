package com.triacompany.academic.deduplication;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface BibliographicDuplicateReviewRepository
        extends JpaRepository<BibliographicDuplicateReview, UUID> {

    List<BibliographicDuplicateReview> findByResearcherIdOrderBySimilarityScoreDescCreatedAtDesc(
            UUID researcherId
    );

    Optional<BibliographicDuplicateReview> findByResearcherIdAndLeftSourceAndLeftWorkIdAndRightSourceAndRightWorkId(
            UUID researcherId,
            String leftSource,
            UUID leftWorkId,
            String rightSource,
            UUID rightWorkId
    );
}
