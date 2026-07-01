package com.maismaes.com.br.dto.request;

import jakarta.validation.constraints.NotNull;

public record ResponderPedidoRequestDTO(@NotNull Boolean aprovado) {}

