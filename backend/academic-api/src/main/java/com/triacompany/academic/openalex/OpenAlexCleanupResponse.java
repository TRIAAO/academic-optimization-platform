package com.triacompany.academic.openalex;

import java.util.UUID;

public record OpenAlexCleanupResponse(
        UUID researcherId,
        String researcherName,
        Integer deletedWorks,
        String message
) {
}