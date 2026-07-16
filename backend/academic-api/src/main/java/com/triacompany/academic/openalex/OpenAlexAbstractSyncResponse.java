package com.triacompany.academic.openalex;

import java.time.LocalDateTime;
import java.util.UUID;

public record OpenAlexAbstractSyncResponse(
        UUID researcherId,
        String researcherName,
        int totalFound,
        int matchedImportedWorks,
        int updatedWorks,
        int worksWithAbstract,
        LocalDateTime syncedAt
) {
}
