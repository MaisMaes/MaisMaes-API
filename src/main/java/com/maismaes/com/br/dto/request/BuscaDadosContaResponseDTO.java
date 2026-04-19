package com.maismaes.com.br.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public record BuscaDadosContaResponseDTO(
        String nome,
        String email,
        String telefone,
        String role
) {
}
