package com.triacompany.academic.scientometrics;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

public record ScientometricAnalysisResponse(
        UUID researcherId,
        String researcherName,
        LocalDate latestSnapshotDate,
        int measurementCount,
        DIndexAssessment dIndexAssessment,
        VitalityAssessment vitalityAssessment,
        InstitutionalEmailAssessment institutionalEmailAssessment,
        List<ScientometricAlert> alerts
) {

    public record DIndexAssessment(
            Integer hIndex,
            Integer dIndex,
            Integer deviationPercent,
            String status,
            String explanation
    ) {
    }

    public record VitalityAssessment(
            Integer score,
            String status,
            Integer citationsRecentPercent,
            Integer hIndexRecentPercent,
            Integer i10IndexRecentPercent,
            String trend,
            Integer citationsDelta,
            Integer hIndexDelta,
            Integer i10IndexDelta,
            String explanation
    ) {
    }

    public record InstitutionalEmailAssessment(
            String email,
            String domain,
            Boolean declaredVerified,
            Boolean domainRecognized,
            String status,
            String explanation
    ) {
    }

    public record ScientometricAlert(
            String code,
            String severity,
            String title,
            String message,
            String action
    ) {
    }
}
