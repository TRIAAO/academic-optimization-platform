package com.triacompany.academic.dashboard;

import java.time.LocalDateTime;
import java.util.List;

public record InstitutionalDashboardResponse(
        String institutionName,

        Integer totalResearchers,
        Integer activeResearchers,

        Integer totalProfiles,
        Integer completeProfiles,
        Integer incompleteProfiles,

        Integer totalOrcidWorks,
        Integer totalOpenAlexWorks,
        Integer confirmedOpenAlexWorks,
        Integer pendingReviewOpenAlexWorks,
        Integer rejectedOpenAlexWorks,

        Integer totalCrossrefValidations,
        Integer doiConfirmedCount,
        Integer doiMissingCount,
        Integer doiNotFoundCount,

        Integer excellentResearchers,
        Integer goodResearchers,
        Integer optimizingResearchers,
        Integer criticalResearchers,

        Integer averageScore,
        String institutionalStatus,
        String executiveSummary,

        List<DashboardMetricResponse> metrics,
        List<DashboardResearcherSummaryResponse> researchers,

        LocalDateTime generatedAt
) {
}