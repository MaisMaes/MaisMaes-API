package com.maismaes.com.br.dto.request;

import jakarta.validation.constraints.NotNull;

public record CriarDenunciaGrupoRequestDTO(
    @NotNull(message = "O motivo da denúncia é obrigatório") String descricao) {}
