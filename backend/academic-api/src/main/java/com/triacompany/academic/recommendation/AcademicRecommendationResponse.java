package com.triacompany.academic.recommendation;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

public record AcademicRecommendationResponse(
        UUID researcherId,
        String researcherName,
        String researchArea,
        int evidenceScore,
        String evidenceLevel,
        RecommendationEvidenceResponse evidence,
        List<KeywordRecommendationResponse> keywords,
        List<CollaboratorRecommendationResponse> collaborators,
        List<JournalRecommendationResponse> journals,
        List<AcademicRecommendationActionResponse> nextActions,
        String methodology,
        String googleScholarPolicy,
        LocalDateTime generatedAt
) {
}
