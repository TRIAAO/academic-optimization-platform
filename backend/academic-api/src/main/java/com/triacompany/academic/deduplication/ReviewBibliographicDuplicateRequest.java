package com.triacompany.academic.deduplication;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record ReviewBibliographicDuplicateRequest(
        @NotNull DuplicateReviewStatus status,
        @Size(max = 2000) String note
) {
}
