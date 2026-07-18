package com.triacompany.academic.deduplication;

import java.util.List;
import java.util.UUID;

public record BibliographicDeduplicationResponse(
        UUID researcherId,
        int totalCandidates,
        int pendingCandidates,
        int confirmedCandidates,
        int rejectedCandidates,
        List<BibliographicDuplicateCandidateResponse> candidates
) {
}
