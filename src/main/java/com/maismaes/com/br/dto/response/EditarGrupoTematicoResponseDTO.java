package com.maismaes.com.br.dto.response;

import java.util.List;

import com.maismaes.com.br.entities.grupo_tematico.Bairro;
import com.maismaes.com.br.entities.grupo_tematico.GrupoTematico;

public record EditarGrupoTematicoResponseDTO(
    Long id,
    String titulo,
    List<String> bairros,
    String criadoraNome
) {
    public EditarGrupoTematicoResponseDTO(GrupoTematico grupo) {
        this(
            grupo.getId(),
            grupo.getTitulo(),
            grupo.getBairros().stream().map(Bairro::getNome).toList(),
            grupo.getCriador().getNome()
        );
    }
}
