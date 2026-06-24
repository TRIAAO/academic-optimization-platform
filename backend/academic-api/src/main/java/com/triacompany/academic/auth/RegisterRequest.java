package com.triacompany.academic.auth;

import com.triacompany.academic.user.UserRole;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record RegisterRequest(

        @NotBlank(message = "O nome completo é obrigatório.")
        @Size(max = 180, message = "O nome completo deve ter no máximo 180 caracteres.")
        String fullName,

        @NotBlank(message = "O e-mail é obrigatório.")
        @Email(message = "Informe um e-mail válido.")
        @Size(max = 180, message = "O e-mail deve ter no máximo 180 caracteres.")
        String email,

        @NotBlank(message = "A senha é obrigatória.")
        @Size(min = 6, max = 120, message = "A senha deve ter entre 6 e 120 caracteres.")
        String password,

        UserRole role
) {
}