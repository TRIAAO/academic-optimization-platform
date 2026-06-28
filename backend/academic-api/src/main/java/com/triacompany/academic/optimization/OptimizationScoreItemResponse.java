package com.triacompany.academic.optimization;

public record OptimizationScoreItemResponse(
        String code,
        String label,
        Integer score,
        Integer maxScore,
        String status,
        String message
) {
}