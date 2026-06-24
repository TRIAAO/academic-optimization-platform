package com.triacompany.academic.researcher;

import jakarta.validation.constraints.Size;

public record UpdateResearcherRequest(

        @Size(max = 180, message = "O nome completo deve ter no máximo 180 caracteres.")
        String fullName,

        @Size(max = 50, message = "O telefone deve ter no máximo 50 caracteres.")
        String phone,

        @Size(max = 180, message = "A instituição deve ter no máximo 180 caracteres.")
        String institution,

        @Size(max = 180, message = "O departamento deve ter no máximo 180 caracteres.")
        String department,

        @Size(max = 120, message = "O título acadêmico deve ter no máximo 120 caracteres.")
        String academicTitle,

        @Size(max = 50, message = "O ORCID deve ter no máximo 50 caracteres.")
        String orcidId
) {
}