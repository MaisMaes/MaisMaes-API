package com.maismaes.com.br.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

public record RedefinirSenhaRequestDTO(
        @NotBlank(message = "Email é obrigatório")
        @Email(message = "Email inválido")
        String email,

        @NotBlank(message = "Código é obrigatório")
        @Pattern(regexp = "^\\d{6}$", message = "Código deve conter 6 dígitos numéricos")
        String codigo,

        @NotBlank(message = "Senha é obrigatória")
        @Pattern(regexp = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[@#$%^&+=!]).{8,}$",
                message = "Senha não atende aos critérios de segurança")
        String novaSenha
) {
}

