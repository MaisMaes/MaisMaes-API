package com.maismaes.com.br.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public record CriarDenunciaMulherRequestDTO(
    @NotBlank(message = "Descrição não pode estar vazia") String descricao) {}
