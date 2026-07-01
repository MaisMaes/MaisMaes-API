package com.maismaes.com.br.dto.response;

import java.util.List;

public record DetalheGrupoResponseDTO(
    Long id,
    String titulo,
    String descricao,
    String categoria,
    boolean privado,
    Integer numeroParticipantes,
    Integer tempoEntreMensagens,
    boolean video,
    boolean audio,
    boolean imagem,
    boolean documento,
    List<String> bairros,
    List<ParticipanteGrupoResumoResponseDTO> participantes,
    boolean usuarioLogadoEParticipante,
    String usuarioLogadoRole,
    boolean usuarioLogadoFavoritou,
    boolean usuarioLogadoAguardandoAprovacao) {}
