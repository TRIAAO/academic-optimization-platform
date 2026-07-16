package com.triacompany.academic.openalex;

import jakarta.validation.constraints.NotBlank;

public record OpenAlexAuthorIdentityRequest(
        @NotBlank(message = "Author ID OpenAlex é obrigatório.")
        String openAlexAuthorId
) {
}
