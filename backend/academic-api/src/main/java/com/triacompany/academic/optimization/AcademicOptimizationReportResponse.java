package com.triacompany.academic.optimization;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

public record AcademicOptimizationReportResponse(
        UUID researcherId,
        String researcherName,
        String researcherEmail,
        String institution,
        String department,
        String academicTitle,
        String orcidId,

        Integer profileCompletionPercentage,
        Integer totalOrcidWorks,
        Integer totalOpenAlexWorks,
        Integer confirmedOpenAlexWorks,
        Integer pendingReviewOpenAlexWorks,
        Integer rejectedOpenAlexWorks,

        Integer totalCrossrefValidations,
        Integer doiConfirmedCount,
        Integer highConfidenceCount,
        Integer possibleMatchCount,
        Integer doiMissingCount,
        Integer doiNotFoundCount,
        Integer errorCount,

        Integer overallScore,
        String overallStatus,
        String executiveSummary,

        List<OptimizationScoreItemResponse> scoreItems,
        List<OptimizationRecommendationResponse> recommendations,

        LocalDateTime generatedAt
) {
}