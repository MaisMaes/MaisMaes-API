package com.maismaes.com.br.dto.response;

import com.maismaes.com.br.entities.Delegacia;

public record DelegaciaProximaResponseDTO(
    Long id,
    String nome,
    String endereco,
    String telefone,
    double latitude,
    double longitude,
    double distanciaKm) {

  public static DelegaciaProximaResponseDTO of(Delegacia d, double distanciaKm) {
    return new DelegaciaProximaResponseDTO(
        d.getId(),
        d.getNome(),
        d.getEndereco(),
        d.getTelefone(),
        d.getLatitude(),
        d.getLongitude(),
        distanciaKm);
  }
}