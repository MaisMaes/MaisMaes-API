package com.maismaes.com.br.repository;

import com.maismaes.com.br.entities.Delegacia;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface DelegaciaRepository extends JpaRepository<Delegacia, Long> {

  // Pré-filtro por coordenadas (lat/lon).
  List<Delegacia> findByLatitudeBetweenAndLongitudeBetween(
      double latMin, double latMax, double lonMin, double lonMax);
}