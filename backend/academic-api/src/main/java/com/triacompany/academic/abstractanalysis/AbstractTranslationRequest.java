package com.triacompany.academic.abstractanalysis;

import jakarta.validation.constraints.Size;

public record AbstractTranslationRequest(
        @Size(max = 30000, message = "O abstract em português deve ter no máximo 30000 caracteres.")
        String abstractPt,

        @Size(max = 30000, message = "O abstract em inglês deve ter no máximo 30000 caracteres.")
        String abstractEn
) {
}
