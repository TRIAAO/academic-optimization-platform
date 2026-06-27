package com.triacompany.academic.openalex;

import jakarta.validation.constraints.Size;

public record OpenAlexWorkReviewRequest(
        @Size(max = 1000, message = "A observação da revisão deve ter no máximo 1000 caracteres.")
        String reviewNote
) {
}