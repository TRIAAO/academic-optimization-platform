package com.triacompany.academic.editorial;

import java.util.UUID;

public record EditorialWorkResponse(
        UUID workId,
        String title,
        Integer publicationYear,
        String sourceName,
        String abstractLanguage
) {
}
