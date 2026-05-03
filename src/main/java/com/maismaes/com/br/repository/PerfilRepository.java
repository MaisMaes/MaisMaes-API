package com.maismaes.com.br.repository;

import com.maismaes.com.br.entities.Perfil;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PerfilRepository extends JpaRepository<Perfil, UUID> {

  Perfil findByPerfilEmail(String email);
}
