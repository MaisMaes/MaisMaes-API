package com.maismaes.com.br.dto.response;

import com.maismaes.com.br.entities.grupo_tematico.Bairro;
import com.maismaes.com.br.entities.grupo_tematico.GrupoTematico;
import com.maismaes.com.br.entities.grupo_tematico.ParticipanteGrupo;
import java.util.List;
import java.util.UUID;

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
    int qtdParticipantesAtual,
    boolean banido) {
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
        grupo.getParticipantes() != null
            ? (int) grupo.getParticipantes().stream().filter(ParticipanteGrupo::isAtivo).count()
            : 0,
        false);
  }

  public ListarGrupoTematicoDTO(GrupoTematico grupo, UUID usuarioLogadoId) {
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
        grupo.getParticipantes() != null
            ? (int) grupo.getParticipantes().stream().filter(ParticipanteGrupo::isAtivo).count()
            : 0,
        verificarSeBanido(grupo, usuarioLogadoId));
  }

  private static boolean verificarSeBanido(GrupoTematico grupo, UUID usuarioLogadoId) {
    return grupo.getParticipantes().stream()
        .anyMatch(
            p ->
                p.getUsuario().getId().equals(usuarioLogadoId)
                    && !p.isAtivo()
                    && p.getMotivoBanimento() != null);
  }
}
