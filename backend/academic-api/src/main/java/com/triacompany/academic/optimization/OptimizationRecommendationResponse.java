package com.triacompany.academic.optimization;

public record OptimizationRecommendationResponse(
        String priority,
        String area,
        String recommendation
) {
}