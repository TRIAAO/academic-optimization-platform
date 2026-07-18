package com.triacompany.academic.scientometrics;

import java.time.LocalDate;

public record InstitutionalScientometricHistoryPoint(
        LocalDate periodStart,
        LocalDate periodEnd,
        Integer researchersMeasured,
        Integer metricCoverageRate,
        Long citationsTotal,
        Long citationsLastSixYears,
        Double averageHIndexTotal,
        Double averageHIndexLastSixYears,
        Double averageDIndex,
        Double averageI10IndexTotal,
        Double averageI10IndexLastSixYears,
        Integer institutionalEmailVerifiedResearchers,
        Integer institutionalEmailVerificationRate
) {
}
