package com.maismaes.com.br.dto.request;

import com.maismaes.com.br.entities.grupo_tematico.StatusDenuncia;


public record DenunciaGrupoFilterDTO(
        StatusDenuncia status,
        Long grupoId,
        String usuarioId
) {
}
