package com.maismaes.com.br.dto.response;

import com.maismaes.com.br.entities.grupo_tematico.Bairro;
import com.maismaes.com.br.entities.grupo_tematico.GrupoTematico;
import java.util.List;

public record EditarGrupoTematicoResponseDTO(
    Long id, String titulo, List<String> bairros, String criadoraNome) {
  public EditarGrupoTematicoResponseDTO(GrupoTematico grupo) {
    this(
        grupo.getId(),
        grupo.getTitulo(),
        grupo.getBairros().stream().map(Bairro::getNome).toList(),
        grupo.getCriador().getNome());
  }
}
