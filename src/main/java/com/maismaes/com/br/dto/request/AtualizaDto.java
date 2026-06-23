package com.maismaes.com.br.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "DTO utilizado para atualização de um infocard")
public record AtualizaDto(
    @Schema(description = "Título do infocard", example = "Auxílio Jurídico Gratuito atualizado")
        String titulo,
    @Schema(description = "Descricao do infocard", example = "Descricaoa tualizada")
        String descricao,
    @Schema(description = "Imagem do infocard", example = "ww.imagem.atualizada") String imagem,
    @Schema(description = "Link infocard", example = "ww.link.atualizado") String link,
    @Schema(description = "Atualiza status de destaque", example = "true") Boolean destaque,
    @Schema(description = "Ativa ou nao um, infocard", example = "true") Boolean ativo) {}
