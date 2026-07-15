package com.triacompany.academic.scientometrics;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

public record ScientometricMetricResponse(
        UUID id,
        UUID researcherId,
        String researcherName,
        String researcherEmail,
        String source,
        String googleScholarAuthorId,
        String googleScholarProfileUrl,
        Integer hIndexTotal,
        Integer hIndexLastSixYears,
        Integer i10IndexTotal,
        Integer i10IndexLastSixYears,
        Integer citationsTotal,
        Integer citationsLastSixYears,
        Integer dIndex,
        String verifiedEmail,
        Boolean institutionalEmailVerified,
        String interests,
        String notes,
        LocalDate snapshotDate,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
    public static ScientometricMetricResponse fromEntity(ScientometricMetric metric) {
        return new ScientometricMetricResponse(
                metric.getId(),
                metric.getResearcher().getId(),
                metric.getResearcher().getFullName(),
                metric.getResearcher().getEmail(),
                metric.getSource(),
                metric.getGoogleScholarAuthorId(),
                metric.getGoogleScholarProfileUrl(),
                metric.getHIndexTotal(),
                metric.getHIndexLastSixYears(),
                metric.getI10IndexTotal(),
                metric.getI10IndexLastSixYears(),
                metric.getCitationsTotal(),
                metric.getCitationsLastSixYears(),
                metric.getDIndex(),
                metric.getVerifiedEmail(),
                metric.getInstitutionalEmailVerified(),
                metric.getInterests(),
                metric.getNotes(),
                metric.getSnapshotDate(),
                metric.getCreatedAt(),
                metric.getUpdatedAt()
        );
    }
}