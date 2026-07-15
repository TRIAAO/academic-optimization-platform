package com.triacompany.academic.scientometrics;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.PastOrPresent;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;

public record CreateScientometricMetricRequest(

        @Size(max = 80, message = "A fonte deve ter no máximo 80 caracteres.")
        String source,

        @Size(max = 120, message = "O Author ID do Google Acadêmico deve ter no máximo 120 caracteres.")
        String googleScholarAuthorId,

        @Size(max = 500, message = "A URL do perfil Google Acadêmico deve ter no máximo 500 caracteres.")
        String googleScholarProfileUrl,

        @PositiveOrZero(message = "O H-index total não pode ser negativo.")
        Integer hIndexTotal,

        @PositiveOrZero(message = "O H-index dos últimos 6 anos não pode ser negativo.")
        Integer hIndexLastSixYears,

        @PositiveOrZero(message = "O i10-index total não pode ser negativo.")
        Integer i10IndexTotal,

        @PositiveOrZero(message = "O i10-index dos últimos 6 anos não pode ser negativo.")
        Integer i10IndexLastSixYears,

        @PositiveOrZero(message = "O total de citações não pode ser negativo.")
        Integer citationsTotal,

        @PositiveOrZero(message = "As citações dos últimos 6 anos não podem ser negativas.")
        Integer citationsLastSixYears,

        @PositiveOrZero(message = "O D-index não pode ser negativo.")
        Integer dIndex,

        @Email(message = "Informe um e-mail verificado válido.")
        @Size(max = 180, message = "O e-mail verificado deve ter no máximo 180 caracteres.")
        String verifiedEmail,

        Boolean institutionalEmailVerified,

        String interests,

        String notes,

        @PastOrPresent(message = "A data da medição não pode estar no futuro.")
        LocalDate snapshotDate
) {
}