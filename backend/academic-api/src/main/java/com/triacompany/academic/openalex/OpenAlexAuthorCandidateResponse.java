package com.triacompany.academic.openalex;

public record OpenAlexAuthorCandidateResponse(
        String openAlexAuthorId,
        String openAlexAuthorShortId,
        String orcid,
        String displayName,
        String lastKnownInstitution,
        String lastKnownCountryCode,
        Integer worksCount,
        Integer citedByCount,
        Double relevanceScore
) {
}