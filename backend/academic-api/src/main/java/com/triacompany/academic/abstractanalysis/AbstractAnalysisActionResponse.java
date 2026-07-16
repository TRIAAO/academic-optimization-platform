package com.triacompany.academic.abstractanalysis;

public record AbstractAnalysisActionResponse(
        int priority,
        String title,
        String description,
        String targetModule
) {
}
