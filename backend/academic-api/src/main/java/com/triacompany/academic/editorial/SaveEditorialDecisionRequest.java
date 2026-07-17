package com.triacompany.academic.editorial;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.util.List;

public record SaveEditorialDecisionRequest(
        @NotBlank(message = "O nome do periódico é obrigatório.")
        @Size(max = 500, message = "O nome do periódico deve ter no máximo 500 caracteres.")
        String journalName,

        @Size(max = 500, message = "A editora deve ter no máximo 500 caracteres.")
        String publisher,

        @Size(max = 10, message = "Informe no máximo 10 ISSNs.")
        List<@Size(max = 30, message = "Cada ISSN deve ter no máximo 30 caracteres.") String> issns,

        @Min(value = 0, message = "O score deve ser maior ou igual a zero.")
        @Max(value = 100, message = "O score deve ser menor ou igual a 100.")
        int relevanceScore,

        @Size(max = 1000, message = "A URL oficial deve ter no máximo 1000 caracteres.")
        String officialUrl,

        @NotNull(message = "O status da decisão é obrigatório.")
        EditorialDecisionStatus status,

        boolean scopeConfirmed,
        boolean peerReviewConfirmed,
        boolean indexingConfirmed,
        boolean feesConfirmed,
        boolean languageConfirmed,
        boolean deadlinesConfirmed,

        @Size(max = 2000, message = "As observações devem ter no máximo 2000 caracteres.")
        String notes
) {
}
