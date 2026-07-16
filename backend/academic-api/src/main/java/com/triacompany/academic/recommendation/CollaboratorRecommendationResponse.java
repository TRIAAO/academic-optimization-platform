package com.triacompany.academic.recommendation;

public record CollaboratorRecommendationResponse(
        String displayName,
        String openAlexAuthorId,
        String orcid,
        String institution,
        int sharedWorks,
        int sharedCitations,
        Integer latestCollaborationYear,
        int relevanceScore,
        String confidence,
        String rationale
) {
}
