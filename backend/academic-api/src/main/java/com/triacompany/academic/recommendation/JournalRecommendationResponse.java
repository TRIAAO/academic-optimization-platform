package com.triacompany.academic.recommendation;

public record JournalRecommendationResponse(
        String journalName,
        int relatedWorks,
        int totalCitations,
        int openAccessWorks,
        int validatedDoiWorks,
        int relevanceScore,
        String confidence,
        String rationale
) {
}
