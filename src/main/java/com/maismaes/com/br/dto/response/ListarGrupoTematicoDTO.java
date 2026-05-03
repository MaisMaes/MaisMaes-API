package com.maismaes.com.br.dto.response;

import com.maismaes.com.br.entities.grupo_tematico.Bairro;
import com.maismaes.com.br.entities.grupo_tematico.GrupoTematico;
import java.util.List;

public record ListarGrupoTematicoDTO(
    Long id,
    String titulo,
    String descricao,
    String categoria,
    String nomeCriador,
    boolean privado,
    Integer numeroParticipantes,
    Integer tempoEntreMensagens,
    boolean video,
    boolean audio,
    boolean imagem,
    boolean documento,
    List<String> bairros,
    int qtdParticipantesAtual) {
  public ListarGrupoTematicoDTO(GrupoTematico grupo) {
    this(
        grupo.getId(),
        grupo.getTitulo(),
        grupo.getDescricao(),
        grupo.getCategorias() != null ? grupo.getCategorias().name() : null,
        grupo.getCriador() != null ? grupo.getCriador().getNome() : "Sem Criador",
        grupo.isPrivado(),
        grupo.getNumeroParticipantes(),
        grupo.getTempoEntreMensagens(),
        grupo.isVideo(),
        grupo.isAudio(),
        grupo.isImagem(),
        grupo.isDocumento(),
        grupo.getBairros().stream().map(Bairro::getNome).toList(),
        grupo.getParticipantes() != null ? grupo.getParticipantes().size() : 0);
  }
}
