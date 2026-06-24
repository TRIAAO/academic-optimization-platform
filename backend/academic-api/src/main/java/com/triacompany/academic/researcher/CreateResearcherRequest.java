package com.triacompany.academic.researcher;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record CreateResearcherRequest(

        @NotBlank(message = "O nome completo é obrigatório.")
        @Size(max = 180, message = "O nome completo deve ter no máximo 180 caracteres.")
        String fullName,

        @NotBlank(message = "O e-mail é obrigatório.")
        @Email(message = "Informe um e-mail válido.")
        @Size(max = 180, message = "O e-mail deve ter no máximo 180 caracteres.")
        String email,

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