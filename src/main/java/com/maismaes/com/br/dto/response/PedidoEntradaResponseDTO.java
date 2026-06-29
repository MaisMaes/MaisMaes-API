package com.maismaes.com.br.dto.response;

import com.maismaes.com.br.entities.grupo_tematico.PedidoEntradaGrupo;
import java.time.LocalDateTime;
import java.util.UUID;

public record PedidoEntradaResponseDTO(
    Long pedidoId,
    Long grupoId,
    String nomeGrupo,
    UUID usuarioId,
    String nomeUsuario,
    String status,
    LocalDateTime dataPedido,
    LocalDateTime dataResposta) {

  public PedidoEntradaResponseDTO(PedidoEntradaGrupo pedido) {
    this(
        pedido.getId(),
        pedido.getGrupo().getId(),
        pedido.getGrupo().getTitulo(),
        pedido.getUsuario().getId(),
        pedido.getUsuario().getNome(),
        pedido.getStatus().name(),
        pedido.getDataPedido(),
        pedido.getDataResposta());
  }
}

