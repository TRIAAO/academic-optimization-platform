package com.triacompany.academic.editorial;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

public record EditorialRecommendationResponse(
        UUID researcherId,
        String researcherName,
        int confirmedWorks,
        int worksWithAbstract,
        List<EditorialWorkResponse> eligibleWorks,
        UUID selectedWorkId,
        String selectedWorkTitle,
        String selectedWorkLanguage,
        String selectedWorkSource,
        int candidateJournalCount,
        String evidenceLevel,
        boolean crossrefAvailable,
        String statusMessage,
        List<EditorialJournalRecommendationResponse> journals,
        String methodology,
        String decisionPolicy,
        LocalDateTime generatedAt
) {
}
