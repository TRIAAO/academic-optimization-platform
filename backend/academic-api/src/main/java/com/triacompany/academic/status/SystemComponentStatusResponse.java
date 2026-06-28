package com.triacompany.academic.status;

public record SystemComponentStatusResponse(
        String name,
        String status,
        String message,
        Long responseTimeMs
) {
}