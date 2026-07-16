package com.triacompany.academic.abstractanalysis;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

public record AbstractAnalysisResponse(
        UUID researcherId,
        String researcherName,
        int totalOpenAlexWorks,
        int confirmedWorks,
        int worksWithAbstract,
        int missingAbstracts,
        int abstractCoveragePercentage,
        int portugueseCoveragePercentage,
        int englishCoveragePercentage,
        String evidenceLevel,
        List<AbstractThemeResponse> themes,
        List<AbstractWorkAnalysisResponse> works,
        List<AbstractAnalysisActionResponse> nextActions,
        String methodology,
        String translationPolicy,
        LocalDateTime generatedAt
) {
}
