package com.maismaes.com.br.dto.request;

import com.maismaes.com.br.entities.grupo_tematico.ConsistenciaDenuncia;
import com.maismaes.com.br.entities.grupo_tematico.StatusDenuncia;

public record AtualizarDenunciaDTO(
        String status,
        String descricao,
        String verdadeira
) {}