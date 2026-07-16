package com.triacompany.academic.recommendation;

public record RecommendationEvidenceResponse(
        int profileCompletionPercentage,
        int orcidWorks,
        int openAlexWorks,
        int confirmedOpenAlexWorks,
        int crossrefValidations,
        boolean scientometricSnapshotAvailable,
        boolean openAlexEnrichmentAvailable
) {
}
