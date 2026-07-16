package com.triacompany.academic.recommendation;

public record AcademicRecommendationActionResponse(
        String priority,
        String area,
        String title,
        String description,
        String targetModule
) {
}
