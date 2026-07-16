package com.triacompany.academic.editorial;

import java.util.List;

public record EditorialJournalRecommendationResponse(
        String journalName,
        String publisher,
        List<String> issns,
        int relatedWorks,
        int relatedCitations,
        int relevanceScore,
        String confidence,
        boolean presentInResearcherHistory,
        int maximumTitleSimilarityPercentage,
        String sampleTitle,
        String sampleDoi,
        String sampleUrl,
        String rationale
) {
}
