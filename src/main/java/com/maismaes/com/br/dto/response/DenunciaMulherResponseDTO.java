package com.maismaes.com.br.dto.response;

import com.maismaes.com.br.entities.denuncia.DenunciaMulher;
import com.maismaes.com.br.entities.denuncia.StatusDenunciaMulher;
import java.time.LocalDateTime;

public record DenunciaMulherResponseDTO(
    Long id,
    String nomeUsuario,
    String email,
    String endereco,
    String contato,
    StatusDenunciaMulher status,
    String descricao,
    LocalDateTime dataDenuncia,
    LocalDateTime atualizadoEm) {

  public static DenunciaMulherResponseDTO fromEntity(DenunciaMulher denuncia) {
    return new DenunciaMulherResponseDTO(
        denuncia.getId(),
        denuncia.getNomeUsuario(),
        denuncia.getEmail(),
        denuncia.getEndereco(),
        denuncia.getContato(),
        denuncia.getStatus(),
        denuncia.getDescricao(),
        denuncia.getDataDenuncia(),
        denuncia.getAtualizadoEm());
  }
}
