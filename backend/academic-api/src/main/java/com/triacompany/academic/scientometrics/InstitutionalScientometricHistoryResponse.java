package com.triacompany.academic.scientometrics;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

public record InstitutionalScientometricHistoryResponse(
        Integer requestedMonths,
        Integer staleAfterMonths,
        LocalDate periodStart,
        LocalDate periodEnd,
        LocalDate latestSnapshotDate,
        Long totalActiveResearchers,
        Long researchersWithMetrics,
        Long researchersWithoutMetrics,
        Long researchersWithoutRecentMetrics,
        Integer currentMetricCoverageRate,
        Integer currentInstitutionalEmailVerificationRate,
        LocalDateTime generatedAt,
        List<InstitutionalScientometricHistoryPoint> timeline
) {
}
