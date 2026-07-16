package com.triacompany.academic.recommendation;

public record KeywordRecommendationResponse(
        String keyword,
        int evidenceCount,
        int relevanceScore,
        String confidence,
        String source,
        String rationale
) {
}
