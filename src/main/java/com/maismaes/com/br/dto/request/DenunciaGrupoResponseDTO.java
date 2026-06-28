package com.maismaes.com.br.dto.request;

import com.maismaes.com.br.entities.grupo_tematico.ConsistenciaDenuncia;
import com.maismaes.com.br.entities.grupo_tematico.DenunciarGrupo;
import java.time.LocalDateTime;
import java.util.UUID;

public record DenunciaGrupoResponseDTO(
    Long id,
    String descricao,
    String status,
    Long grupoId,
    String grupoNome,
    UUID usuarioId,
    String usuarioNome,
    LocalDateTime abertoEm,
    LocalDateTime atualizadoEm,
    ConsistenciaDenuncia verdadeira) {
  public DenunciaGrupoResponseDTO(DenunciarGrupo denuncia) {
    this(
        denuncia.getId(),
        denuncia.getDescricao(),
        denuncia.getStatus() != null ? denuncia.getStatus().name() : null,
        denuncia.getGrupo() != null ? denuncia.getGrupo().getId() : null,
        denuncia.getGrupo() != null ? denuncia.getGrupo().getTitulo() : null,
        denuncia.getUsuario() != null ? denuncia.getUsuario().getId() : null,
        denuncia.getUsuario() != null ? denuncia.getUsuario().getNome() : null,
        denuncia.getAtualizadoEm(),
        denuncia.getAtualizadoEm(),
        denuncia.getVerdadeira());
  }
}
