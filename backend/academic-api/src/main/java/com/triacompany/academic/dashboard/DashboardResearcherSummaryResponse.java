package com.triacompany.academic.dashboard;

import java.util.UUID;

public record DashboardResearcherSummaryResponse(
        UUID researcherId,
        String researcherName,
        String institution,
        String department,
        String academicTitle,
        String orcidId,
        Integer profileCompletionPercentage,
        Integer totalOrcidWorks,
        Integer totalOpenAlexWorks,
        Integer confirmedOpenAlexWorks,
        Integer pendingReviewOpenAlexWorks,
        Integer doiConfirmedCount,
        Integer overallScore,
        String overallStatus
) {
}