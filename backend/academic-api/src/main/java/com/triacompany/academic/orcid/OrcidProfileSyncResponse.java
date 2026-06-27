package com.triacompany.academic.orcid;

import com.triacompany.academic.profile.AcademicProfileResponse;

import java.util.UUID;

public record OrcidProfileSyncResponse(
        UUID researcherId,
        String researcherName,
        String orcidId,
        String orcidUrl,
        Boolean profileCreated,
        Boolean biographySynced,
        Boolean keywordsSynced,
        Boolean websiteSynced,
        Boolean researchAreaSynced,
        Integer profileCompletionPercentage,
        AcademicProfileResponse academicProfile,
        OrcidProfileSummaryResponse orcidSummary
) {
}