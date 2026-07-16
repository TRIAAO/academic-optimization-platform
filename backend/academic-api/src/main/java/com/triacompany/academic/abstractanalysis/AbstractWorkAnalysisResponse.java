package com.triacompany.academic.abstractanalysis;

import java.time.LocalDateTime;
import java.util.UUID;

public record AbstractWorkAnalysisResponse(
        UUID workId,
        String title,
        Integer publicationYear,
        String sourceName,
        String openAlexUrl,
        String originalLanguage,
        String originalAbstract,
        String abstractPt,
        String abstractEn,
        boolean hasOriginalAbstract,
        boolean hasPortugueseVersion,
        boolean hasEnglishVersion,
        String translationStatus,
        LocalDateTime translationsUpdatedAt
) {
}
