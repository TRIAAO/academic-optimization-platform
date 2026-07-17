package com.triacompany.academic.deduplication;

import java.util.UUID;

public record BibliographicWorkResponse(
        String source,
        UUID id,
        String externalId,
        String title,
        String doi,
        Integer publicationYear,
        String venue,
        Integer citations
) {
}
