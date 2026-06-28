package com.maismaes.com.br.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.util.UUID;

public record BanirParticipanteRequestDTO(
        @NotNull(message = "O usuário é obrigatório.")
        UUID usuarioId,

        @NotBlank(message = "O motivo do banimento é obrigatório.")
        @Size(max = 100, message = "O motivo deve ter no máximo 100 caracteres.")
        String motivo
){
}
