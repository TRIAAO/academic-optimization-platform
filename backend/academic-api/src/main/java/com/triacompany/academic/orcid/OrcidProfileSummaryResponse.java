package com.triacompany.academic.orcid;

import java.util.List;
import java.util.UUID;

public record OrcidProfileSummaryResponse(
        UUID researcherId,
        String researcherName,
        String orcidId,
        String orcidUrl,
        String givenNames,
        String familyName,
        String creditName,
        String displayName,
        String biography,
        List<String> keywords,
        List<OrcidWebsiteResponse> websites,
        List<OrcidAffiliationResponse> employments,
        List<OrcidAffiliationResponse> educations,
        Integer worksCount
) {
}