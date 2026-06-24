package com.triacompany.academic.profile;

import jakarta.validation.constraints.Size;

public record UpdateAcademicProfileRequest(

        @Size(max = 180, message = "A área de pesquisa deve ter no máximo 180 caracteres.")
        String researchArea,

        String biography,

        String keywords,

        @Size(max = 255, message = "A URL do Google Acadêmico deve ter no máximo 255 caracteres.")
        String googleScholarUrl,

        @Size(max = 255, message = "A URL do ORCID deve ter no máximo 255 caracteres.")
        String orcidUrl,

        @Size(max = 100, message = "O Scopus Author ID deve ter no máximo 100 caracteres.")
        String scopusAuthorId,

        @Size(max = 100, message = "O Web of Science ID deve ter no máximo 100 caracteres.")
        String webOfScienceId,

        @Size(max = 255, message = "A URL do Lattes deve ter no máximo 255 caracteres.")
        String lattesUrl,

        @Size(max = 255, message = "A URL do perfil institucional deve ter no máximo 255 caracteres.")
        String institutionalProfileUrl
) {
}