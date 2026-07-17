package com.triacompany.academic.deduplication;

import java.time.LocalDateTime;
import java.util.UUID;

public record BibliographicDuplicateCandidateResponse(
        UUID id,
        UUID researcherId,
        BibliographicWorkResponse leftWork,
        BibliographicWorkResponse rightWork,
        Integer similarityScore,
        Integer titleSimilarity,
        Boolean doiExactMatch,
        Boolean publicationYearCompatible,
        String rationale,
        DuplicateReviewStatus reviewStatus,
        String reviewerNote,
        String reviewedBy,
        LocalDateTime reviewedAt,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
}
