package com.maismaes.com.br.service;

import com.maismaes.com.br.dto.response.DelegaciaProximaResponseDTO;
import com.maismaes.com.br.repository.DelegaciaRepository;
import java.util.Comparator;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

@Service
@RequiredArgsConstructor
public class DelegaciaService {

  private static final double RAIO_MAX_KM = 50;
  private static final int LIMITE_MAX = 50;
  private static final double KM_POR_GRAU = 111.32;

  private final DelegaciaRepository delegaciaRepository;

  public List<DelegaciaProximaResponseDTO> buscarProximas(
      double lat, double lon, double raioKm, int limite) {

    if (lat < -90 || lat > 90 || lon < -180 || lon > 180) {
      throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Coordenadas inválidas");
    }

    double raio = Math.min(Math.max(raioKm, 0.1), RAIO_MAX_KM);
    int max = Math.min(Math.max(limite, 1), LIMITE_MAX);

    // Caixa que contém o círculo de busca
    double deltaLat = raio / KM_POR_GRAU;
    double deltaLon = raio / (KM_POR_GRAU * Math.max(Math.cos(Math.toRadians(lat)), 0.01));

    return delegaciaRepository
        .findByLatitudeBetweenAndLongitudeBetween(
            lat - deltaLat, lat + deltaLat, lon - deltaLon, lon + deltaLon)
        .stream()
        .map(
            d ->
                DelegaciaProximaResponseDTO.of(
                    d, distanciaKm(lat, lon, d.getLatitude(), d.getLongitude())))
        .filter(d -> d.distanciaKm() <= raio)
        .sorted(Comparator.comparingDouble(DelegaciaProximaResponseDTO::distanciaKm))
        .limit(max)
        .toList();
  }

  // Fórmula de Haversine (distância entre dois pontos na esfera, em km)
  private static double distanciaKm(double lat1, double lon1, double lat2, double lon2) {
    double dLat = Math.toRadians(lat2 - lat1);
    double dLon = Math.toRadians(lon2 - lon1);
    double a =
        Math.pow(Math.sin(dLat / 2), 2)
            + Math.cos(Math.toRadians(lat1))
                * Math.cos(Math.toRadians(lat2))
                * Math.pow(Math.sin(dLon / 2), 2);
    return 6371 * 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
  }
}