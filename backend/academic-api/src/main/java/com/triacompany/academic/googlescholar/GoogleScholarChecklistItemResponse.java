package com.triacompany.academic.googlescholar;

public record GoogleScholarChecklistItemResponse(
        String code,
        String title,
        String description,
        GoogleScholarChecklistStatus status,
        String priority,
        String manualAction
) {
}