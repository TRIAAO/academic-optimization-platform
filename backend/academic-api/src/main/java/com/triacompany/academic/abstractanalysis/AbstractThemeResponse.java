package com.triacompany.academic.abstractanalysis;

public record AbstractThemeResponse(
        String theme,
        int abstractCount,
        int occurrenceCount,
        int relevanceScore,
        String confidence,
        String source,
        String rationale
) {
}
