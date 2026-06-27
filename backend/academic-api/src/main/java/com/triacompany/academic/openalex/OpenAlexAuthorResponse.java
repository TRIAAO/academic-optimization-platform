package com.triacompany.academic.openalex;

public record OpenAlexAuthorResponse(
        String openAlexAuthorId,
        String orcid,
        String displayName,
        Integer worksCount,
        Integer citedByCount
) {
}