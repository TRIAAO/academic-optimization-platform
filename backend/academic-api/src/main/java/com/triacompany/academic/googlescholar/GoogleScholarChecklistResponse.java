package com.triacompany.academic.googlescholar;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

public record GoogleScholarChecklistResponse(
        UUID researcherId,
        String researcherName,
        String researcherEmail,
        String institution,
        String department,
        String academicTitle,
        String orcidId,
        String googleScholarUrl,

        Integer profileCompletionPercentage,
        Integer totalOrcidWorks,
        Integer totalOpenAlexWorks,
        Integer confirmedOpenAlexWorks,
        Integer pendingReviewOpenAlexWorks,
        Integer totalCrossrefValidations,
        Integer doiConfirmedCount,
        Integer doiMissingCount,

        Integer completedItems,
        Integer needsReviewItems,
        Integer missingItems,
        Integer totalItems,
        Integer checklistScore,
        String checklistStatus,

        String summary,
        String importantNotice,

        List<GoogleScholarChecklistItemResponse> items,

        LocalDateTime generatedAt
) {
}