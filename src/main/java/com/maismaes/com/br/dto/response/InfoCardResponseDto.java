package com.maismaes.com.br.dto.response;

import com.maismaes.com.br.entities.Infocard;
import io.swagger.v3.oas.annotations.media.Schema;
import java.time.LocalDateTime;
import java.util.UUID;

public record InfoCardResponseDto(
    @Schema(description = "Identificador único do infocard") UUID id,
    @Schema(description = "Título do infocard") String titulo,
    @Schema(description = "Descrição do infocard") String descricao,
    @Schema(description = "Imagem do infocard") String imagem,
    @Schema(description = "Link externo do infocard") String link,
    @Schema(description = "Indica se o infocard está em destaque") boolean destaque,
    @Schema(description = "Indica se o infocard está ativo") boolean ativo,
    @Schema(description = "Data de criação") LocalDateTime dataCriacao,
    @Schema(description = "Data da última atualização") LocalDateTime dataAtualizacao) {

  public InfoCardResponseDto(Infocard infocard) {
    this(
        infocard.getId(),
        infocard.getTitulo(),
        infocard.getDescricao(),
        infocard.getImagem(),
        infocard.getLink(),
        infocard.isDestaque(),
        infocard.isAtivo(),
        infocard.getDataCriacao(),
        infocard.getDataAtualizacao());
  }
}
